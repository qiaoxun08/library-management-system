package com.library.config;

import jakarta.servlet.*;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * XSS 防护过滤器
 *
 * 过滤所有请求参数中的 XSS 攻击代码：
 * - <script>...</script>
 * - javascript:
 * - onerror= onload= 等事件属性
 * - <iframe> <object> <embed> 等危险标签
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class XssFilter implements Filter {

    // XSS 攻击模式
    private static final Pattern[] XSS_PATTERNS = {
            Pattern.compile("<script[^>]*?>[\\s\\S]*?</script>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("javascript\\s*:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<iframe[^>]*?>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<object[^>]*?>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<embed[^>]*?>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<svg[^>]*?>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("expression\\s*\\(", Pattern.CASE_INSENSITIVE),
            Pattern.compile("eval\\s*\\(", Pattern.CASE_INSENSITIVE),
            Pattern.compile("alert\\s*\\(", Pattern.CASE_INSENSITIVE),
            Pattern.compile("document\\.cookie", Pattern.CASE_INSENSITIVE),
            Pattern.compile("document\\.domain", Pattern.CASE_INSENSITIVE),
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        chain.doFilter(new XssRequestWrapper(httpRequest), response);
    }

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    /**
     * 清理 JSON body 中的 XSS：解析 JSON 后递归清洗所有字符串值，
     * 不破坏 JSON 结构（不能对原始 JSON 文本直接 cleanXss，会转义引号导致解析失败）
     */
    static String cleanJsonBody(String body) {
        if (body == null || body.isEmpty()) {
            return body;
        }
        try {
            JsonNode root = JSON_MAPPER.readTree(body);
            if (root instanceof ObjectNode obj) {
                cleanJsonNode(obj);
            } else if (root instanceof ArrayNode arr) {
                for (int i = 0; i < arr.size(); i++) {
                    JsonNode child = arr.get(i);
                    if (child instanceof ObjectNode o) {
                        cleanJsonNode(o);
                    } else if (child instanceof TextNode) {
                        arr.set(i, TextNode.valueOf(cleanXss(child.asText())));
                    }
                }
            } else {
                return body; // 非对象/数组的 JSON（极少见），原样放行
            }
            return JSON_MAPPER.writeValueAsString(root);
        } catch (Exception e) {
            // 解析失败（可能不是合法 JSON，如文件上传 multipart），原样放行交给后续校验
            return body;
        }
    }

    private static void cleanJsonNode(ObjectNode obj) {
        java.util.Iterator<java.util.Map.Entry<String, JsonNode>> fields = obj.fields();
        while (fields.hasNext()) {
            java.util.Map.Entry<String, JsonNode> entry = fields.next();
            JsonNode value = entry.getValue();
            if (value instanceof TextNode) {
                entry.setValue(TextNode.valueOf(cleanXss(value.asText())));
            } else if (value instanceof ObjectNode nested) {
                cleanJsonNode(nested);
            } else if (value instanceof ArrayNode arr) {
                for (int i = 0; i < arr.size(); i++) {
                    JsonNode child = arr.get(i);
                    if (child instanceof TextNode) {
                        arr.set(i, TextNode.valueOf(cleanXss(child.asText())));
                    } else if (child instanceof ObjectNode o) {
                        cleanJsonNode(o);
                    }
                }
            }
        }
    }

    /**
     * 清理 XSS 攻击代码
     * 先转义 HTML 特殊字符，再过滤危险 pattern
     */
    public static String cleanXss(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        // 先转义 HTML 特殊字符（防止注入）
        value = value.replace("&", "&amp;")
                     .replace("<", "&lt;")
                     .replace(">", "&gt;")
                     .replace("\"", "&quot;")
                     .replace("'", "&#x27;");
        // 再过滤危险 pattern（如 javascript:、onerror= 等）
        for (Pattern pattern : XSS_PATTERNS) {
            value = pattern.matcher(value).replaceAll("");
        }
        return value;
    }

    /**
     * 请求包装器，对参数和 JSON body 进行 XSS 过滤
     */
    private static class XssRequestWrapper extends HttpServletRequestWrapper {

        private byte[] cachedJsonBody; // JSON body 过滤后的缓存（InputStream 只能读一次）

        public XssRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            String contentType = request.getContentType();
            if (contentType != null && contentType.toLowerCase().contains("application/json")) {
                try (InputStream in = request.getInputStream()) {
                    String body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                    this.cachedJsonBody = cleanJsonBody(body).getBytes(StandardCharsets.UTF_8);
                }
            }
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            if (cachedJsonBody == null) {
                return super.getInputStream();
            }
            ByteArrayInputStream in = new ByteArrayInputStream(cachedJsonBody);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() { return in.available() == 0; }
                @Override
                public boolean isReady() { return true; }
                @Override
                public void setReadListener(ReadListener listener) { }
                @Override
                public int read() { return in.read(); }
            };
        }

        @Override
        public java.io.BufferedReader getReader() throws IOException {
            if (cachedJsonBody == null) {
                return super.getReader();
            }
            return new java.io.BufferedReader(new java.io.InputStreamReader(
                    new ByteArrayInputStream(cachedJsonBody), StandardCharsets.UTF_8));
        }

        @Override
        public String getParameter(String name) {
            String value = super.getParameter(name);
            return cleanXss(value);
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null) {
                return null;
            }
            String[] cleanedValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                cleanedValues[i] = cleanXss(values[i]);
            }
            return cleanedValues;
        }

        @Override
        public String getQueryString() {
            String queryString = super.getQueryString();
            return cleanXss(queryString);
        }
    }
}

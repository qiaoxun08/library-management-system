package com.library.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
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
     * 请求包装器，对参数进行 XSS 过滤
     */
    private static class XssRequestWrapper extends HttpServletRequestWrapper {

        public XssRequestWrapper(HttpServletRequest request) {
            super(request);
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

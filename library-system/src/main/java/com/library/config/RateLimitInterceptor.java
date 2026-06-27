package com.library.config;

import com.library.dto.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 基于 Redis 的 IP 限流拦截器
 * 使用固定窗口计数器算法
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private com.library.service.RedisCacheService redisCacheService;

    /**
     * 检查 IP 限流
     * @param key 限流 key（如 "rate_limit:login"）
     * @param maxRequests 窗口期内最大请求数
     * @param windowMinutes 窗口期（分钟）
     */
    private boolean isRateLimited(String ip, String key, int maxRequests, int windowMinutes) {
        String redisKey = "rate_limit:" + key + ":" + ip;
        Long count = redisCacheService.increment(redisKey, windowMinutes);
        return count != null && count > maxRequests;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = getClientIp(request);
        String uri = request.getRequestURI();

        // 登录接口：每分钟最多 10 次
        if (uri.contains("/auth/login")) {
            if (isRateLimited(ip, "login", 10, 1)) {
                log.warn("登录限流触发: ip={}, uri={}", ip, uri);
                sendRateLimitResponse(response, "登录请求过于频繁，请1分钟后再试");
                return false;
            }
        }

        // 书评发布接口：每分钟最多 5 次
        if (uri.contains("/reviews") && "POST".equals(request.getMethod())) {
            if (isRateLimited(ip, "review", 5, 1)) {
                log.warn("书评限流触发: ip={}, uri={}", ip, uri);
                sendRateLimitResponse(response, "书评发布过于频繁，请1分钟后再试");
                return false;
            }
        }

        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private void sendRateLimitResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = Result.error(429, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}

package com.library.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * CORS (跨域资源共享) 配置
 * 
 * 问题背景：
 * 前端运行在 localhost:5173，后端运行在 localhost:8080
 * 浏览器的同源策略会阻止不同端口之间的请求
 * 
 * 解决方案：
 * 后端设置 CORS 响应头，告诉浏览器允许跨域请求
 * 
 * 预检请求（Preflight）：
 * 浏览器在发送非简单请求前，会先发 OPTIONS 请求询问服务器是否允许
 * 本配置允许所有来源、所有方法、所有头，开发环境方便调试
 * 生产环境应限制为具体域名
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许的来源（前端地址）
        // 从配置读取，生产环境应设置为具体域名
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));
        
        // 允许的请求头
        // 需要允许 Authorization 头来传递 JWT Token
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 允许的 HTTP 方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // 允许携带凭证（Cookie、Authorization 头等）
        // 设为 true 时，allowedOrigins 不能为 *
        configuration.setAllowCredentials(true);
        
        // 预检请求缓存时间（秒）
        // 浏览器在此时间内不会重复发送 OPTIONS 请求
        configuration.setMaxAge(3600L);

        // 注册 CORS 配置到所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

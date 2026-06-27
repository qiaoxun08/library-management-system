package com.library.config;

import com.library.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器
 * 
 * 工作流程：
 * 1. 从请求头 Authorization: Bearer <token> 中提取 JWT
 * 2. 解析 Token 获取用户名和角色
 * 3. 构建 Spring Security 认证对象
 * 4. 设置到 SecurityContext 中
 * 
 * 这样后续的 Controller 就可以通过 @PreAuthorize 进行权限判断
 * 
 * 注意：这个过滤器在每个请求上都会执行（OncePerRequestFilter）
 * 即使 Token 无效也不会阻断请求，只是不设置认证信息
 * 具体的权限检查由 Spring Security 的授权过滤器完成
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 从请求头获取 Token
        String header = request.getHeader("Authorization");

        // 检查是否是 Bearer Token
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);  // 去掉 "Bearer " 前缀
            try {
                // 解析 Token 获取用户信息
                String username = jwtUtil.getUsernameFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);

                // 如果用户名有效且当前没有认证信息，则设置认证
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Spring Security 需要 ROLE_ 前缀的角色名
                    String authority = "ROLE_" + role.toUpperCase();
                    log.debug("JWT auth: username={}, role={}, authority={}", username, role, authority);
                    
                    // 创建认证对象（不包含密码，因为已经验证过了）
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority(authority))
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 设置到 SecurityContext，后续请求可直接获取用户信息
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // Token 无效（过期、篡改等），不设置认证，让后续权限检查失败
                log.warn("JWT token validation failed: {}", e.getMessage());
            }
        }

        // 继续执行后续过滤器
        filterChain.doFilter(request, response);
    }
}

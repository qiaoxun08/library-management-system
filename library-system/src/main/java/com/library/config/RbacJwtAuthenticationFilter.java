package com.library.config;

import com.library.entity.SysPermission;
import com.library.service.RbacService;
import com.library.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT + RBAC 认证过滤器
 *
 * 流程：
 * 1. 解析 JWT Token 获取 userId 和 userType
 * 2. 从数据库加载用户的角色和权限
 * 3. 构建 SecurityContext，包含 authorities（权限标识）
 */
@Component
public class RbacJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RbacJwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RbacService rbacService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (StringUtils.hasText(token) && !jwtUtil.isTokenExpired(token)) {
            try {
                Claims claims = jwtUtil.getAllClaimsFromToken(token);
                String username = claims.getSubject();
                String userType = claims.get("userType", String.class);
                Integer userId = claims.get("userId", Integer.class);

                if (username != null && userType != null && userId != null) {
                    // 加载 RBAC 权限
                    List<SimpleGrantedAuthority> authorities = loadAuthorities(userType, userId);

                    // 构建认证对象
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("RBAC 认证成功: user={}, type={}, perms={}", username, userType, authorities.size());
                }
            } catch (Exception e) {
                log.warn("RBAC 认证失败: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从 Request 中解析 Token
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 加载用户权限列表
     *
     * 将 RBAC 权限转换为 Spring Security 的 Authority：
     * - 角色 → ROLE_xxx（保持向后兼容）
     * - 权限 → perm_key（细粒度权限）
     */
    private List<SimpleGrantedAuthority> loadAuthorities(String userType, Integer userId) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // 1. 添加角色（保持向后兼容，如 ROLE_ADMIN, ROLE_READER）
        var roles = rbacService.getUserRoles(userType, userId);
        for (var role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleKey()));
        }

        // 2. 添加细粒度权限（如 book:create, borrow:return）
        var permissions = rbacService.getUserPermissions(userType, userId);
        for (SysPermission perm : permissions) {
            authorities.add(new SimpleGrantedAuthority(perm.getPermKey()));
        }

        return authorities;
    }
}

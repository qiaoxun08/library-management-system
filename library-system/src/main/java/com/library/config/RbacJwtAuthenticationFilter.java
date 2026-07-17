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

                if (username != null) {
                    List<SimpleGrantedAuthority> authorities;

                    if (userType != null && userId != null) {
                        // 新版 Token：从 RBAC 表加载权限
                        authorities = loadAuthorities(userType, userId);
                    } else {
                        // 兼容旧版 Token（只有 role 字段）：直接用 role 构建权限
                        String role = claims.get("role", String.class);
                        userType = role; // 用于 fallback
                        authorities = new ArrayList<>();
                        if (role != null) {
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                            // admin 自动获得 ROLE_ADMIN（兼容 SUPER_ADMIN）
                            if ("admin".equalsIgnoreCase(role)) {
                                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                                authorities.add(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
                            }
                        }
                        log.info("旧版 Token 兼容: user={}, role={}, 请重新登录获取完整 RBAC 权限", username, role);
                    }

                    // 构建认证对象
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("认证成功: user={}, type={}, perms={}", username, userType, authorities.size());
                }
            } catch (Exception e) {
                log.warn("认证失败: {}", e.getMessage());
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
     *
     * 如果 RBAC 表无数据（sys_user_role 未初始化），自动 fallback 到 userType 对应的默认角色
     */
    private List<SimpleGrantedAuthority> loadAuthorities(String userType, Integer userId) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        try {
            // 1. 从 RBAC 表加载角色
            var roles = rbacService.getUserRoles(userType, userId);
            for (var role : roles) {
                String roleKey = role.getRoleKey();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + roleKey));
                // SUPER_ADMIN 向下兼容：自动添加 ROLE_ADMIN，避免大量 @PreAuthorize 修改
                if ("SUPER_ADMIN".equals(roleKey)) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                }
            }

            // 2. 从 RBAC 表加载细粒度权限
            var permissions = rbacService.getUserPermissions(userType, userId);
            for (SysPermission perm : permissions) {
                authorities.add(new SimpleGrantedAuthority(perm.getPermKey()));
            }
        } catch (Exception e) {
            log.warn("RBAC 表查询失败，将使用默认角色: {}", e.getMessage());
        }

        // 3. Fallback：如果 RBAC 表无数据，根据 userType 添加默认角色
        if (authorities.isEmpty()) {
            String defaultRole = mapDefaultRole(userType);
            if (defaultRole != null) {
                authorities.add(new SimpleGrantedAuthority(defaultRole));
                log.info("RBAC fallback: user={}, type={}, assigned default role={}", userId, userType, defaultRole);
            }
        }

        return authorities;
    }

    /**
     * userType → 默认 ROLE 映射（RBAC 表无数据时的 fallback）
     */
    private String mapDefaultRole(String userType) {
        if (userType == null) return null;
        return switch (userType.toUpperCase()) {
            case "ADMIN" -> "ROLE_ADMIN";
            case "LIBRARIAN" -> "ROLE_LIBRARIAN";
            case "READER" -> "ROLE_READER";
            default -> null;
        };
    }
}

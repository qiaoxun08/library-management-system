package com.library.aspect;

import com.library.annotation.RequirePermission;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 权限校验AOP切面
 * 拦截@RequirePermission注解，校验当前用户是否拥有指定权限
 *
 * 依赖RbacJwtAuthenticationFilter加载的权限列表
 * 权限标识格式：book:create, borrow:return等
 */
@Aspect
@Component
public class PermissionAspect {

    private static final Logger log = LoggerFactory.getLogger(PermissionAspect.class);

    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        // 获取当前用户
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("未登录或登录已过期");
        }

        String username = auth.getName();

        // 获取需要的权限列表
        String[] permissions = requirePermission.value().split(",");
        List<String> requiredPermissions = Arrays.asList(permissions);

        // 从SecurityContext获取当前用户的权限列表
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();

        // 校验权限
        boolean hasPermission;
        if (requirePermission.allRequired()) {
            // 需要所有权限都满足
            hasPermission = requiredPermissions.stream()
                    .allMatch(perm -> authorities.stream()
                            .anyMatch(auth2 -> perm.equals(auth2.getAuthority())));
        } else {
            // 只需要其中一个权限
            hasPermission = requiredPermissions.stream()
                    .anyMatch(perm -> authorities.stream()
                            .anyMatch(auth2 -> perm.equals(auth2.getAuthority())));
        }

        if (!hasPermission) {
            String errorMsg = String.format("无权限访问: 需要权限 %s", requiredPermissions);
            log.warn("权限校验失败: user={}, required={}", username, requiredPermissions);
            throw new AccessDeniedException(errorMsg);
        }

        log.debug("权限校验通过: user={}, required={}", username, requiredPermissions);
        return joinPoint.proceed();
    }
}

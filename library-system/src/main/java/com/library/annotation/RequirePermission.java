package com.library.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * 用于Controller方法上，标注该方法需要的细粒度权限
 *
 * 示例：
 * @RequirePermission("book:create")
 * @RequirePermission("borrow:return")
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    /**
     * 权限标识，如 "book:create", "borrow:return"
     */
    String value();

    /**
     * 是否需要所有权限都满足（默认true）
     * true: 需要所有权限
     * false: 只需要其中一个权限
     */
    boolean allRequired() default true;
}

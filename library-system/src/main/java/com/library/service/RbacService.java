package com.library.service;

import com.library.entity.SysPermission;
import com.library.entity.SysRole;

import java.util.List;
import java.util.Set;

/**
 * RBAC 权限服务
 */
public interface RbacService {

    /**
     * 查询用户拥有的角色列表
     * @param userType 用户类型：READER/ADMIN/LIBRARIAN
     * @param userId 用户ID
     */
    List<SysRole> getUserRoles(String userType, Integer userId);

    /**
     * 查询用户拥有的权限列表
     * @param userType 用户类型
     * @param userId 用户ID
     */
    List<SysPermission> getUserPermissions(String userType, Integer userId);

    /**
     * 查询用户拥有的权限标识集合（用于 JWT 和 SecurityContext）
     * @param userType 用户类型
     * @param userId 用户ID
     */
    Set<String> getUserPermKeys(String userType, Integer userId);

    /**
     * 检查用户是否拥有某个权限
     */
    boolean hasPermission(String userType, Integer userId, String permKey);

    /**
     * 检查用户是否拥有某个角色
     */
    boolean hasRole(String userType, Integer userId, String roleKey);
}

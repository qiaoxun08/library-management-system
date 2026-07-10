package com.library.service.impl;

import com.library.entity.SysPermission;
import com.library.entity.SysRole;
import com.library.mapper.SysPermissionMapper;
import com.library.mapper.SysRoleMapper;
import com.library.service.RbacService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * RBAC 权限服务实现
 */
@Service
public class RbacServiceImpl implements RbacService {

    private static final Logger log = LoggerFactory.getLogger(RbacServiceImpl.class);

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    @Override
    public List<SysRole> getUserRoles(String userType, Integer userId) {
        return sysRoleMapper.findRolesByUser(userType, userId);
    }

    @Override
    public List<SysPermission> getUserPermissions(String userType, Integer userId) {
        return sysPermissionMapper.findPermissionsByUser(userType, userId);
    }

    @Override
    public Set<String> getUserPermKeys(String userType, Integer userId) {
        List<SysPermission> permissions = getUserPermissions(userType, userId);
        Set<String> permKeys = permissions.stream()
                .map(SysPermission::getPermKey)
                .collect(Collectors.toSet());
        log.debug("getUserPermKeys: userType={}, userId={}, permKeys={}", userType, userId, permKeys);
        return permKeys;
    }

    @Override
    public boolean hasPermission(String userType, Integer userId, String permKey) {
        Set<String> permKeys = getUserPermKeys(userType, userId);
        return permKeys.contains(permKey);
    }

    @Override
    public boolean hasRole(String userType, Integer userId, String roleKey) {
        List<SysRole> roles = getUserRoles(userType, userId);
        return roles.stream().anyMatch(r -> r.getRoleKey().equals(roleKey));
    }
}

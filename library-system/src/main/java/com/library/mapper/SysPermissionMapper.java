package com.library.mapper;

import com.library.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysPermissionMapper {
    SysPermission findById(@Param("id") Integer id);
    SysPermission findByPermKey(@Param("permKey") String permKey);
    List<SysPermission> findAll();
    int insert(SysPermission permission);
    int update(SysPermission permission);
    int delete(@Param("id") Integer id);

    /**
     * 查询用户拥有的权限列表（通过角色关联）
     */
    List<SysPermission> findPermissionsByUser(@Param("userType") String userType, @Param("userId") Integer userId);

    /**
     * 查询角色拥有的权限列表
     */
    List<SysPermission> findPermissionsByRoleId(@Param("roleId") Integer roleId);
}

package com.library.mapper;

import com.library.entity.SysRolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRolePermissionMapper {
    List<SysRolePermission> findByRoleId(@Param("roleId") Integer roleId);
    int insert(SysRolePermission rolePermission);
    int deleteByRoleId(@Param("roleId") Integer roleId);
    int deleteByPermissionId(@Param("permissionId") Integer permissionId);
}

package com.library.mapper;

import com.library.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserRoleMapper {
    List<SysUserRole> findByUser(@Param("userType") String userType, @Param("userId") Integer userId);
    int insert(SysUserRole userRole);
    int deleteByUser(@Param("userType") String userType, @Param("userId") Integer userId);
    int deleteByRoleId(@Param("roleId") Integer roleId);
}

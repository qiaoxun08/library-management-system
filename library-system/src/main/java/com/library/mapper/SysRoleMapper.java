package com.library.mapper;

import com.library.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRoleMapper {
    SysRole findById(@Param("id") Integer id);
    SysRole findByRoleKey(@Param("roleKey") String roleKey);
    List<SysRole> findAll();
    int insert(SysRole role);
    int update(SysRole role);
    int delete(@Param("id") Integer id);

    /**
     * 查询用户拥有的角色列表
     */
    List<SysRole> findRolesByUser(@Param("userType") String userType, @Param("userId") Integer userId);
}

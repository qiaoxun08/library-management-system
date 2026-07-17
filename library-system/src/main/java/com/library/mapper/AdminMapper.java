package com.library.mapper;

import com.library.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminMapper {
    Admin findByUsername(@Param("username") String username);
    List<Admin> findAll();
    int insert(Admin admin);
    int update(Admin admin);
    int delete(@Param("id") Integer id);
    String findPasswordById(@Param("id") Integer id);
    int updatePassword(@Param("id") Integer id, @Param("password") String password);
}

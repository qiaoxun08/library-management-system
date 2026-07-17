package com.library.mapper;

import com.library.entity.LoginLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 登录审计日志Mapper
 */
@Mapper
public interface LoginLogMapper {
    int insert(LoginLog log);
    List<LoginLog> findPaged(@Param("offset") int offset, @Param("size") int size);
    int countAll();
}

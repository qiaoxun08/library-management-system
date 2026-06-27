package com.library.mapper;

import com.library.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作日志Mapper
 */
@Mapper
public interface OperationLogMapper {
    List<OperationLog> findAll();
    List<OperationLog> findPaged(@Param("offset") int offset, @Param("size") int size);
    int countAll();
    List<OperationLog> findByUserId(@Param("userId") Integer userId);
    List<OperationLog> findByModule(@Param("module") String module);
    int insert(OperationLog log);
    int delete(@Param("id") Integer id);
}

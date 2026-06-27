package com.library.mapper;

import com.library.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统参数配置Mapper
 */
@Mapper
public interface SystemConfigMapper {
    List<SystemConfig> findAll();
    SystemConfig findById(@Param("id") Integer id);
    SystemConfig findByKey(@Param("configKey") String configKey);
    int insert(SystemConfig config);
    int update(SystemConfig config);
    int delete(@Param("id") Integer id);
}

package com.library.service;

import com.library.entity.SystemConfig;

import java.util.List;

/**
 * 系统参数配置服务接口
 */
public interface SystemConfigService {
    List<SystemConfig> getAllConfig();
    SystemConfig getConfigByKey(String configKey);
    void updateConfig(SystemConfig config);
    void updateConfigByKey(String configKey, String configValue);
    String getConfigValue(String configKey);
}

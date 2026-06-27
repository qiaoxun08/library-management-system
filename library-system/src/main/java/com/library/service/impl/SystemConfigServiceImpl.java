package com.library.service.impl;

import com.library.entity.SystemConfig;
import com.library.exception.BusinessException;
import com.library.mapper.SystemConfigMapper;
import com.library.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统参数配置服务实现
 */
@Service
public class SystemConfigServiceImpl implements SystemConfigService {

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Override
    public List<SystemConfig> getAllConfig() {
        return systemConfigMapper.findAll();
    }

    @Override
    public SystemConfig getConfigByKey(String configKey) {
        SystemConfig config = systemConfigMapper.findByKey(configKey);
        if (config == null) {
            throw new BusinessException("配置项不存在: " + configKey);
        }
        return config;
    }

    @Override
    public void updateConfig(SystemConfig config) {
        SystemConfig existing = systemConfigMapper.findById(config.getId());
        if (existing == null) {
            throw new BusinessException("配置项不存在");
        }
        systemConfigMapper.update(config);
    }

    @Override
    public void updateConfigByKey(String configKey, String configValue) {
        SystemConfig existing = systemConfigMapper.findByKey(configKey);
        if (existing != null) {
            existing.setConfigValue(configValue);
            systemConfigMapper.update(existing);
        }
    }

    @Override
    public String getConfigValue(String configKey) {
        SystemConfig config = systemConfigMapper.findByKey(configKey);
        return config != null ? config.getConfigValue() : null;
    }
}

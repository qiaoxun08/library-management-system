package com.library.service.impl;

import com.library.entity.SystemConfig;
import com.library.exception.BusinessException;
import com.library.mapper.SystemConfigMapper;
import com.library.service.RedisCacheService;
import com.library.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统参数配置服务实现
 * 配置值使用本地缓存（5分钟），配置变更时清除缓存
 */
@Service
public class SystemConfigServiceImpl implements SystemConfigService {

    private static final String CONFIG_CACHE_PREFIX = "sysconfig:";

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Autowired
    private RedisCacheService redisCacheService;

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
        // 清除缓存
        if (existing.getConfigKey() != null) {
            redisCacheService.delete(CONFIG_CACHE_PREFIX + existing.getConfigKey());
        }
    }

    @Override
    public void updateConfigByKey(String configKey, String configValue) {
        SystemConfig existing = systemConfigMapper.findByKey(configKey);
        if (existing != null) {
            existing.setConfigValue(configValue);
            systemConfigMapper.update(existing);
            // 清除缓存
            redisCacheService.delete(CONFIG_CACHE_PREFIX + configKey);
        }
    }

    @Override
    public String getConfigValue(String configKey) {
        // 先查缓存
        String cached = redisCacheService.get(CONFIG_CACHE_PREFIX + configKey, String.class);
        if (cached != null) {
            return cached;
        }
        // 查数据库
        SystemConfig config = systemConfigMapper.findByKey(configKey);
        String value = config != null ? config.getConfigValue() : null;
        // 写入缓存（5分钟）
        if (value != null) {
            redisCacheService.set(CONFIG_CACHE_PREFIX + configKey, value, 5);
        }
        return value;
    }
}

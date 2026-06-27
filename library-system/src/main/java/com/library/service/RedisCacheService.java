package com.library.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存服务，带本地降级方案
 * Redis 不可用时自动降级到 ConcurrentHashMap 本地缓存
 */
@Service
public class RedisCacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    private final java.util.concurrent.ConcurrentHashMap<String, Object> localCache = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<String, Long> localCacheTime = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 判断 Redis 是否可用
     */
    private boolean isRedisAvailable() {
        if (redisTemplate == null) return false;
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 设置缓存（带 TTL）
     */
    public void set(String key, Object value, long ttlMinutes) {
        if (isRedisAvailable()) {
            try {
                String json = objectMapper.writeValueAsString(value);
                redisTemplate.opsForValue().set(key, json, ttlMinutes, TimeUnit.MINUTES);
                return;
            } catch (JsonProcessingException e) {
                log.error("Redis 序列化失败，降级到本地缓存: key={}", key, e);
            } catch (Exception e) {
                log.warn("Redis 写入失败，降级到本地缓存: key={}", key, e);
            }
        }
        // 降级：写入本地缓存
        localCache.put(key, value);
        localCacheTime.put(key, System.currentTimeMillis() + ttlMinutes * 60 * 1000);
    }

    /**
     * 获取缓存
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        if (isRedisAvailable()) {
            try {
                String json = redisTemplate.opsForValue().get(key);
                if (json != null) {
                    return objectMapper.readValue(json, clazz);
                }
                return null;
            } catch (Exception e) {
                log.warn("Redis 读取失败，降级到本地缓存: key={}", key, e);
            }
        }
        // 降级：从本地缓存读取
        Long expireTime = localCacheTime.get(key);
        if (expireTime != null && System.currentTimeMillis() < expireTime) {
            Object value = localCache.get(key);
            if (clazz.isInstance(value)) {
                return (T) value;
            }
        }
        return null;
    }

    /**
     * 获取缓存（泛型列表）
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, TypeReference<T> typeRef) {
        if (isRedisAvailable()) {
            try {
                String json = redisTemplate.opsForValue().get(key);
                if (json != null) {
                    return objectMapper.readValue(json, typeRef);
                }
                return null;
            } catch (Exception e) {
                log.warn("Redis 读取失败，降级到本地缓存: key={}", key, e);
            }
        }
        // 降级
        Long expireTime = localCacheTime.get(key);
        if (expireTime != null && System.currentTimeMillis() < expireTime) {
            Object value = localCache.get(key);
            return (T) value;
        }
        return null;
    }

    /**
     * 删除缓存
     */
    public void delete(String key) {
        if (isRedisAvailable()) {
            try {
                redisTemplate.delete(key);
            } catch (Exception e) {
                log.warn("Redis 删除失败: key={}", key, e);
            }
        }
        localCache.remove(key);
        localCacheTime.remove(key);
    }

    /**
     * 检查 key 是否存在
     */
    public boolean hasKey(String key) {
        if (isRedisAvailable()) {
            try {
                Boolean exists = redisTemplate.hasKey(key);
                return Boolean.TRUE.equals(exists);
            } catch (Exception e) {
                log.warn("Redis 检查 key 失败: key={}", key, e);
            }
        }
        Long expireTime = localCacheTime.get(key);
        return expireTime != null && System.currentTimeMillis() < expireTime;
    }

    /**
     * Redis 递增（用于计数器/限流）
     */
    public Long increment(String key, long ttlMinutes) {
        if (isRedisAvailable()) {
            try {
                Long count = redisTemplate.opsForValue().increment(key);
                if (count != null && count == 1) {
                    redisTemplate.expire(key, ttlMinutes, TimeUnit.MINUTES);
                }
                return count;
            } catch (Exception e) {
                log.warn("Redis increment 失败: key={}", key, e);
            }
        }
        // 降级：本地原子计数
        Long[] result = {0L};
        localCache.compute(key, (k, v) -> {
            long current = (v instanceof Long) ? (Long) v : 0L;
            result[0] = current + 1;
            return result[0];
        });
        return result[0];
    }
}

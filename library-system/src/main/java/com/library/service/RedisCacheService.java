package com.library.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存服务，带本地二级缓存和空值缓存
 * 1. 本地缓存（Caffeine）→ Redis → 数据库
 * 2. 空值缓存防止缓存穿透
 * 3. Redis 不可用时自动降级到本地缓存
 */
@Service
public class RedisCacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 空值对象标记，用于防止缓存穿透
    private static final Object NULL_OBJECT = new Object();

    // 本地缓存：最大10000个key，5分钟过期
    private final Cache<String, Object> localCache = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

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
        // 写入本地缓存
        localCache.put(key, value);

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
    }

    /**
     * 设置空值缓存（防止缓存穿透）
     * 当数据库查询结果为空时，缓存一个空对象，设置较短的TTL
     */
    public void setNull(String key, long ttlMinutes) {
        localCache.put(key, NULL_OBJECT);

        if (isRedisAvailable()) {
            try {
                redisTemplate.opsForValue().set(key, "NULL", ttlMinutes, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.warn("Redis 写入空值缓存失败: key={}", key, e);
            }
        }
    }

    /**
     * 获取缓存
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        // 1. 先查本地缓存
        Object localValue = localCache.getIfPresent(key);
        if (localValue != null) {
            if (localValue == NULL_OBJECT) {
                return null; // 空值缓存
            }
            if (clazz.isInstance(localValue)) {
                return (T) localValue;
            }
        }

        // 2. 再查 Redis
        if (isRedisAvailable()) {
            try {
                String json = redisTemplate.opsForValue().get(key);
                if (json != null) {
                    if ("NULL".equals(json)) {
                        localCache.put(key, NULL_OBJECT);
                        return null;
                    }
                    T value = objectMapper.readValue(json, clazz);
                    localCache.put(key, value);
                    return value;
                }
            } catch (Exception e) {
                log.warn("Redis 读取失败，降级到本地缓存: key={}", key, e);
            }
        }

        return null;
    }

    /**
     * 获取缓存（泛型列表）
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, TypeReference<T> typeRef) {
        // 1. 先查本地缓存
        Object localValue = localCache.getIfPresent(key);
        if (localValue != null) {
            if (localValue == NULL_OBJECT) {
                return null;
            }
            return (T) localValue;
        }

        // 2. 再查 Redis
        if (isRedisAvailable()) {
            try {
                String json = redisTemplate.opsForValue().get(key);
                if (json != null) {
                    if ("NULL".equals(json)) {
                        localCache.put(key, NULL_OBJECT);
                        return null;
                    }
                    T value = objectMapper.readValue(json, typeRef);
                    localCache.put(key, value);
                    return value;
                }
            } catch (Exception e) {
                log.warn("Redis 读取失败，降级到本地缓存: key={}", key, e);
            }
        }

        return null;
    }

    /**
     * 删除缓存
     */
    public void delete(String key) {
        localCache.invalidate(key);
        if (isRedisAvailable()) {
            try {
                redisTemplate.delete(key);
            } catch (Exception e) {
                log.warn("Redis 删除失败: key={}", key, e);
            }
        }
    }

    /**
     * 检查 key 是否存在
     */
    public boolean hasKey(String key) {
        // 先查本地缓存
        if (localCache.getIfPresent(key) != null) {
            return true;
        }

        if (isRedisAvailable()) {
            try {
                Boolean exists = redisTemplate.hasKey(key);
                return Boolean.TRUE.equals(exists);
            } catch (Exception e) {
                log.warn("Redis 检查 key 失败: key={}", key, e);
            }
        }
        return false;
    }

    /**
     * Redis 递增（用于计数器/限流）
     */
    public Long increment(String key, long ttlMinutes) {
        if (isRedisAvailable()) {
            try {
                Long count = redisTemplate.opsForValue().increment(key);
                if (count != null && count == 1L && key != null) {
                    redisTemplate.expire(key, ttlMinutes, TimeUnit.MINUTES);
                }
                return count;
            } catch (Exception e) {
                log.warn("Redis increment 失败: key={}", key, e);
            }
        }
        // 降级：本地原子计数
        Long[] result = {0L};
        localCache.asMap().compute(key, (k, v) -> {
            long current = (v instanceof Long) ? (Long) v : 0L;
            result[0] = current + 1;
            return result[0];
        });
        return result[0];
    }
}

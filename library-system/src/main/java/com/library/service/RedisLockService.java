package com.library.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis 分布式锁服务
 * 基于 SETNX 实现，支持锁续期和自动释放
 */
@Service
public class RedisLockService {

    private static final Logger log = LoggerFactory.getLogger(RedisLockService.class);

    // Lua 脚本：原子性释放锁（只有持有者才能释放）
    private static final String UNLOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "   return redis.call('del', KEYS[1]) " +
            "else " +
            "   return 0 " +
            "end";

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
     * 尝试获取分布式锁
     *
     * @param lockKey   锁的 key（如 "seat:1:2024-01-01:09:00"）
     * @param timeout   锁持有时间（秒），防止死锁
     * @return 锁标识（用于释放），null 表示获取失败
     */
    public String tryLock(String lockKey, long timeout) {
        if (!isRedisAvailable()) {
            log.warn("Redis 不可用，降级为本地锁（单机可用，分布式无效）");
            return tryLocalLock(lockKey, timeout);
        }

        String lockValue = UUID.randomUUID().toString();
        try {
            Boolean success = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, lockValue, timeout, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(success)) {
                log.debug("获取锁成功: key={}, value={}, timeout={}s", lockKey, lockValue, timeout);
                return lockValue;
            }
            log.debug("获取锁失败: key={}", lockKey);
            return null;
        } catch (Exception e) {
            log.error("Redis 获取锁异常: key={}", lockKey, e);
            return null;
        }
    }

    /**
     * 释放分布式锁
     *
     * @param lockKey   锁的 key
     * @param lockValue 锁标识（tryLock 返回的值）
     * @return 是否释放成功
     */
    public boolean unlock(String lockKey, String lockValue) {
        if (lockValue == null) return false;

        if (!isRedisAvailable()) {
            return unlockLocalLock(lockKey);
        }

        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);
            Long result = redisTemplate.execute(script, Collections.singletonList(lockKey), lockValue);
            boolean success = Long.valueOf(1L).equals(result);
            if (success) {
                log.debug("释放锁成功: key={}", lockKey);
            } else {
                log.warn("释放锁失败（可能已过期或被其他线程获取）: key={}", lockKey);
            }
            return success;
        } catch (Exception e) {
            log.error("Redis 释放锁异常: key={}", lockKey, e);
            return false;
        }
    }

    /**
     * 执行带锁的操作（简化使用）
     *
     * @param lockKey   锁的 key
     * @param timeout   锁持有时间（秒）
     * @param action    要执行的操作
     * @return 操作结果
     * @throws LockAcquisitionException 如果获取锁失败
     */
    public <T> T executeWithLock(String lockKey, long timeout, LockAction<T> action) {
        String lockValue = tryLock(lockKey, timeout);
        if (lockValue == null) {
            throw new LockAcquisitionException("系统繁忙，请稍后重试（座位正在被抢占）");
        }
        try {
            return action.execute();
        } finally {
            unlock(lockKey, lockValue);
        }
    }

    /**
     * 执行带锁的操作（无返回值）
     */
    public void executeWithLock(String lockKey, long timeout, Runnable action) {
        String lockValue = tryLock(lockKey, timeout);
        if (lockValue == null) {
            throw new LockAcquisitionException("系统繁忙，请稍后重试（座位正在被抢占）");
        }
        try {
            action.run();
        } finally {
            unlock(lockKey, lockValue);
        }
    }

    // ==================== 本地锁降级（单机环境） ====================

    private static final java.util.concurrent.ConcurrentHashMap<String, String> localLocks = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.concurrent.ConcurrentHashMap<String, Long> localLockExpiry = new java.util.concurrent.ConcurrentHashMap<>();

    private String tryLocalLock(String lockKey, long timeout) {
        String lockValue = UUID.randomUUID().toString();
        Long expiry = System.currentTimeMillis() + timeout * 1000;

        // 检查是否已有锁且未过期
        String existingValue = localLocks.get(lockKey);
        Long existingExpiry = localLockExpiry.get(lockKey);
        if (existingValue != null && existingExpiry != null && System.currentTimeMillis() < existingExpiry) {
            return null; // 锁被持有且未过期
        }

        // 尝试获取锁
        String result = localLocks.putIfAbsent(lockKey, lockValue);
        if (result == null) {
            localLockExpiry.put(lockKey, expiry);
            return lockValue;
        }
        return null;
    }

    private boolean unlockLocalLock(String lockKey) {
        localLocks.remove(lockKey);
        localLockExpiry.remove(lockKey);
        return true;
    }

    // ==================== 函数式接口 ====================

    @FunctionalInterface
    public interface LockAction<T> {
        T execute();
    }

    // ==================== 自定义异常 ====================

    public static class LockAcquisitionException extends RuntimeException {
        public LockAcquisitionException(String message) {
            super(message);
        }
    }
}

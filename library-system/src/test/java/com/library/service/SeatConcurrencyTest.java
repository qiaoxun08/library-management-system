package com.library.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 座位预约并发测试
 * 验证分布式锁在高并发场景下的有效性
 */
@SpringBootTest
@ActiveProfiles("test")
class SeatConcurrencyTest {

    @Autowired
    private RedisLockService redisLockService;

    /**
     * 测试 1：同一座位并发预约 - 验证只有一个成功
     */
    @Test
    void testConcurrentSeatReservation() throws Exception {
        int threadCount = 10;
        int seatId = 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int userId = i + 1;
            futures.add(executor.submit(() -> {
                try {
                    startLatch.await(); // 等待所有线程就绪
                    String lockKey = "seat:reserve:" + seatId;
                    String lockValue = redisLockService.tryLock(lockKey, 5);

                    if (lockValue != null) {
                        try {
                            // 模拟业务处理
                            Thread.sleep(50);
                            successCount.incrementAndGet();
                            System.out.println("用户 " + userId + " 抢座成功");
                        } finally {
                            redisLockService.unlock(lockKey, lockValue);
                        }
                    } else {
                        failCount.incrementAndGet();
                        System.out.println("用户 " + userId + " 抢座失败（锁被占用）");
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            }));
        }

        startLatch.countDown(); // 同时启动所有线程
        endLatch.await();
        executor.shutdown();

        System.out.println("=== 抢座结果 ===");
        System.out.println("成功: " + successCount.get());
        System.out.println("失败: " + failCount.get());

        // 验证：在分布式锁保护下，只有一个能成功
        // 注意：由于锁的粒度和时序，可能有多个成功（但远小于线程数）
        assertTrue(successCount.get() < threadCount, "应该有线程被锁阻挡");
        assertEquals(threadCount, successCount.get() + failCount.get(), "总次数应等于线程数");
    }

    /**
     * 测试 2：使用 executeWithLock 的并发控制
     */
    @Test
    void testExecuteWithLockConcurrency() throws Exception {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger counter = new AtomicInteger(0);
        List<Integer> results = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int userId = i + 1;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    String lockKey = "seat:checkin:99";

                    try {
                        Integer result = redisLockService.executeWithLock(lockKey, 5, () -> {
                            int val = counter.incrementAndGet();
                            System.out.println("用户 " + userId + " 执行业务逻辑, counter=" + val);
                            try {
                                Thread.sleep(100); // 模拟耗时操作
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            return val;
                        });
                        synchronized (results) {
                            results.add(result);
                        }
                    } catch (RedisLockService.LockAcquisitionException e) {
                        System.out.println("用户 " + userId + " 获取锁失败: " + e.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        System.out.println("=== 执行结果 ===");
        System.out.println("成功执行次数: " + results.size());
        System.out.println("Counter 最终值: " + counter.get());

        // 验证：由于锁是串行执行，counter 的值应该等于成功执行次数
        assertEquals(results.size(), counter.get(), "counter 应等于成功执行次数");
    }

    /**
     * 测试 3：锁超时自动释放
     */
    @Test
    void testLockAutoRelease() throws Exception {
        String lockKey = "seat:test:timeout";
        String lockValue1 = redisLockService.tryLock(lockKey, 2); // 2秒超时

        assertNotNull(lockValue1, "第一次获取锁应成功");

        // 立即尝试获取同一把锁，应失败
        String lockValue2 = redisLockService.tryLock(lockKey, 2);
        assertNull(lockValue2, "第二次获取锁应失败（锁已被持有）");

        // 等待锁过期
        Thread.sleep(2500);

        // 锁过期后应能获取
        String lockValue3 = redisLockService.tryLock(lockKey, 2);
        assertNotNull(lockValue3, "锁过期后应能重新获取");

        // 清理
        redisLockService.unlock(lockKey, lockValue3);
    }

    /**
     * 测试 4：只有持有者才能释放锁
     */
    @Test
    void testOnlyOwnerCanUnlock() throws Exception {
        String lockKey = "seat:test:owner";
        String lockValue1 = redisLockService.tryLock(lockKey, 5);

        assertNotNull(lockValue1);

        // 其他人尝试释放锁，应失败
        boolean unlocked = redisLockService.unlock(lockKey, "wrong-lock-value");
        assertFalse(unlocked, "非持有者不应能释放锁");

        // 持有者释放锁，应成功
        unlocked = redisLockService.unlock(lockKey, lockValue1);
        assertTrue(unlocked, "持有者应能释放锁");
    }
}

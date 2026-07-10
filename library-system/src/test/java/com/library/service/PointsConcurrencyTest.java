package com.library.service;

import com.library.entity.ReaderLevel;
import com.library.mapper.ReaderLevelMapper;
import com.library.mapper.ReaderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 积分并发测试
 * 验证 SELECT FOR UPDATE 悲观锁在高并发场景下的有效性
 */
@SpringBootTest
@ActiveProfiles("test")
class PointsConcurrencyTest {

    @Autowired
    private ReaderLevelService readerLevelService;

    @Autowired
    private ReaderLevelMapper readerLevelMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Integer TEST_READER_ID = 99999;

    @BeforeEach
    void setup() {
        // 确保测试读者存在
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reader WHERE id = ?", Integer.class, TEST_READER_ID);
        if (count == null || count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO reader (id, reader_id, password, real_name, status) VALUES (?, ?, ?, ?, ?)",
                    TEST_READER_ID, "TEST99999", "password", "测试读者", 1);
        }

        // 初始化或重置积分记录
        ReaderLevel existing = readerLevelMapper.findByReaderId(TEST_READER_ID);
        if (existing == null) {
            readerLevelService.initReaderLevel(TEST_READER_ID);
        }
        // 重置积分为 0
        ReaderLevel level = readerLevelMapper.findByReaderId(TEST_READER_ID);
        if (level != null) {
            level.setPoints(0);
            level.setLevel("普通");
            readerLevelMapper.update(level);
        }
    }

    /**
     * 测试 1：并发加分 - 验证积分不丢失
     *
     * 场景：10 个线程同时给同一用户 +10 分
     * 预期：最终积分 = 10 * 10 = 100
     */
    @Test
    void testConcurrentAddPoints() throws Exception {
        int threadCount = 10;
        int pointsPerThread = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    readerLevelService.addPoints(TEST_READER_ID, pointsPerThread);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println("加分失败: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        // 验证最终积分
        ReaderLevel finalLevel = readerLevelMapper.findByReaderId(TEST_READER_ID);
        int expectedPoints = threadCount * pointsPerThread;

        System.out.println("=== 并发加分结果 ===");
        System.out.println("成功次数: " + successCount.get());
        System.out.println("失败次数: " + failCount.get());
        System.out.println("期望积分: " + expectedPoints);
        System.out.println("实际积分: " + finalLevel.getPoints());

        assertEquals(expectedPoints, finalLevel.getPoints(),
                "悲观锁保护下，积分应为 " + expectedPoints + "，实际为 " + finalLevel.getPoints());
    }

    /**
     * 测试 2：并发加减分 - 验证最终结果正确
     *
     * 场景：5 个线程 +10 分，5 个线程 -5 分
     * 预期：最终积分 = 5*10 - 5*5 = 25
     */
    @Test
    void testConcurrentAddAndSubtract() throws Exception {
        int addThreads = 5;
        int subtractThreads = 5;
        int addPoints = 10;
        int subtractPoints = 5;

        ExecutorService executor = Executors.newFixedThreadPool(addThreads + subtractThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(addThreads + subtractThreads);

        AtomicInteger addSuccess = new AtomicInteger(0);
        AtomicInteger subtractSuccess = new AtomicInteger(0);

        // 加分线程
        for (int i = 0; i < addThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    readerLevelService.addPoints(TEST_READER_ID, addPoints);
                    addSuccess.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("加分失败: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // 减分线程
        for (int i = 0; i < subtractThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    readerLevelService.subtractPoints(TEST_READER_ID, subtractPoints);
                    subtractSuccess.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("减分失败: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        // 验证最终积分
        ReaderLevel finalLevel = readerLevelMapper.findByReaderId(TEST_READER_ID);
        int expectedPoints = addThreads * addPoints - subtractThreads * subtractPoints;

        System.out.println("=== 并发加减分结果 ===");
        System.out.println("加分成功: " + addSuccess.get());
        System.out.println("减分成功: " + subtractSuccess.get());
        System.out.println("期望积分: " + expectedPoints);
        System.out.println("实际积分: " + finalLevel.getPoints());

        assertEquals(expectedPoints, finalLevel.getPoints(),
                "悲观锁保护下，最终积分应为 " + expectedPoints);
    }

    /**
     * 测试 3：验证悲观锁的串行化效果
     *
     * 场景：100 个线程同时 +1 分
     * 预期：最终积分 = 100，无丢失
     */
    @Test
    void testPessimisticLockSerialization() throws Exception {
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(10); // 10 个线程竞争
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    readerLevelService.addPoints(TEST_READER_ID, 1);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("操作失败: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        ReaderLevel finalLevel = readerLevelMapper.findByReaderId(TEST_READER_ID);

        System.out.println("=== 悲观锁串行化测试 ===");
        System.out.println("总请求数: " + threadCount);
        System.out.println("成功次数: " + successCount.get());
        System.out.println("期望积分: " + threadCount);
        System.out.println("实际积分: " + finalLevel.getPoints());

        // 悲观锁保证每次只有一个线程修改，积分不应丢失
        assertEquals(threadCount, finalLevel.getPoints(),
                "悲观锁应保证 " + threadCount + " 次 +1 操作后积分等于 " + threadCount);
    }
}

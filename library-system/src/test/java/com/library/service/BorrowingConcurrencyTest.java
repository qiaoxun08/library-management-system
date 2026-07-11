package com.library.service;

import com.library.exception.OptimisticLockException;
import com.library.mapper.BookMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 借阅并发测试
 * 验证乐观锁在"最后一本书"场景下的有效性
 */
@SpringBootTest
@ActiveProfiles("test")
class BorrowingConcurrencyTest {

    @Autowired
    private BorrowingService borrowingService;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Integer TEST_BOOK_ID = 99999;

    @BeforeEach
    void setup() {
        // 使用 JDBC 直接操作，避免 MyBatis 缓存问题
        Integer bookCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM book WHERE id = ?", Integer.class, TEST_BOOK_ID);
        if (bookCount == null || bookCount == 0) {
            jdbcTemplate.update(
                    "INSERT INTO book (id, isbn, title, available_count, total_count, status) VALUES (?, ?, ?, ?, ?, ?)",
                    TEST_BOOK_ID, "978-TEST-" + TEST_BOOK_ID, "并发测试图书", 10, 10, 1);
        } else {
            jdbcTemplate.update(
                    "UPDATE book SET available_count = 10, total_count = 10 WHERE id = ?",
                    TEST_BOOK_ID);
        }

        // 创建测试读者（1000-1009 和 2000-2004）
        for (int i = 1000; i <= 1009; i++) {
            createTestReaderIfNeeded(String.valueOf(i));
        }
        for (int i = 2000; i <= 2004; i++) {
            createTestReaderIfNeeded(String.valueOf(i));
        }
    }

    private void createTestReaderIfNeeded(String readerId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reader WHERE reader_id = ?", Integer.class, readerId);
        if (count == null || count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO reader (reader_id, password, real_name, max_borrow_count, status) VALUES (?, ?, ?, ?, ?)",
                    readerId, "password", "测试读者" + readerId, 5, 1);
        }
    }

    /**
     * 测试 1：最后一本书的并发借阅
     *
     * 场景：10 个线程同时借最后一本书
     * 预期：只有 1 个成功，9 个失败
     *
     * 注意：此测试需要 Redis 环境，H2 内存数据库不支持分布式锁降级后的并发测试
     */
    @Test
    @Disabled("需要 Redis 环境，H2 不支持分布式锁并发测试")
    void testConcurrentBorrowLastBook() throws Exception {
        // 设置库存为 1
        jdbcTemplate.update("UPDATE book SET available_count = 1 WHERE id = ?", TEST_BOOK_ID);

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<String> failMessages = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int userId = 1000 + i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    borrowingService.borrowBook(String.valueOf(userId), TEST_BOOK_ID);
                    successCount.incrementAndGet();
                    System.out.println("用户 " + userId + " 借阅成功");
                } catch (OptimisticLockException e) {
                    failCount.incrementAndGet();
                    synchronized (failMessages) {
                        failMessages.add("用户 " + userId + ": " + e.getMessage());
                    }
                    System.out.println("用户 " + userId + " 借阅失败（乐观锁冲突）: " + e.getMessage());
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    synchronized (failMessages) {
                        failMessages.add("用户 " + userId + ": " + e.getMessage());
                    }
                    System.out.println("用户 " + userId + " 借阅失败: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        System.out.println("=== 并发借阅结果 ===");
        System.out.println("成功次数: " + successCount.get());
        System.out.println("失败次数: " + failCount.get());
        System.out.println("失败信息: " + failMessages);

        // 验证：只有 1 人成功
        assertEquals(1, successCount.get(), "乐观锁应保证只有 1 人借到最后一本书");
        assertEquals(threadCount - 1, failCount.get(), "应有 " + (threadCount - 1) + " 人借阅失败");

        // 验证：库存为 0
        Integer finalStock = jdbcTemplate.queryForObject(
                "SELECT available_count FROM book WHERE id = ?", Integer.class, TEST_BOOK_ID);
        assertEquals(0, finalStock, "库存应为 0");
    }

    /**
     * 测试 2：库存充足的并发借阅
     *
     * 场景：5 个线程借库存为 10 的书
     * 预期：全部成功
     *
     * 注意：此测试需要 Redis 环境，H2 内存数据库不支持分布式锁降级后的并发测试
     */
    @Test
    @Disabled("需要 Redis 环境，H2 不支持分布式锁并发测试")
    void testConcurrentBorrowWithSufficientStock() throws Exception {
        // 设置充足库存
        jdbcTemplate.update("UPDATE book SET available_count = 10 WHERE id = ?", TEST_BOOK_ID);

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int userId = 2000 + i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    borrowingService.borrowBook(String.valueOf(userId), TEST_BOOK_ID);
                    successCount.incrementAndGet();
                    System.out.println("用户 " + userId + " 借阅成功");
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println("用户 " + userId + " 借阅失败: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        System.out.println("=== 库存充足时的并发借阅 ===");
        System.out.println("成功次数: " + successCount.get());
        System.out.println("失败次数: " + failCount.get());

        // 验证：全部成功
        assertEquals(threadCount, successCount.get(), "库存充足时应全部成功");
        assertEquals(0, failCount.get(), "不应有失败");

        // 验证：库存正确扣减
        Integer finalStock = jdbcTemplate.queryForObject(
                "SELECT available_count FROM book WHERE id = ?", Integer.class, TEST_BOOK_ID);
        assertEquals(10 - threadCount, finalStock, "库存应正确扣减");
    }

    /**
     * 测试 3：验证乐观锁 SQL 的原子性
     *
     * 直接调用 decrementAvailableCount，验证 WHERE 条件生效
     */
    @Test
    void testDecrementAvailableCountAtomicity() {
        // 设置库存为 1
        jdbcTemplate.update("UPDATE book SET available_count = 1 WHERE id = ?", TEST_BOOK_ID);

        // 第一次扣减：应成功
        int affected1 = bookMapper.decrementAvailableCount(TEST_BOOK_ID);
        assertEquals(1, affected1, "第一次扣减应成功");

        // 第二次扣减：应失败（库存已为 0）
        int affected2 = bookMapper.decrementAvailableCount(TEST_BOOK_ID);
        assertEquals(0, affected2, "第二次扣减应失败（库存为 0）");

        // 验证库存
        Integer finalStock = jdbcTemplate.queryForObject(
                "SELECT available_count FROM book WHERE id = ?", Integer.class, TEST_BOOK_ID);
        assertEquals(0, finalStock, "库存应为 0");
    }
}

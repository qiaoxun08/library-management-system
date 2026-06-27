package com.library.integration;

import com.library.entity.Blacklist;
import com.library.entity.Reader;
import com.library.exception.BusinessException;
import com.library.mapper.ReaderMapper;
import com.library.service.BlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 黑名单流程集成测试
 * 覆盖：添加黑名单 → 判断是否在黑名单 → 移出黑名单 → 违规累计自动加入
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("黑名单流程集成测试")
class BlacklistFlowIntegrationTest {

    @Autowired
    private BlacklistService blacklistService;

    @Autowired
    private ReaderMapper readerMapper;

    private Reader testReader;

    @BeforeEach
    void setUp() {
        testReader = new Reader();
        testReader.setReaderId("BL001");
        testReader.setPassword("password");
        testReader.setRealName("黑名单测试读者");
        testReader.setDepartment("计算机学院");
        testReader.setMaxBorrowCount(5);
        testReader.setCurrentBorrowCount(0);
        testReader.setFineAmount(java.math.BigDecimal.ZERO);
        testReader.setStatus(1);
        readerMapper.insert(testReader);
        // 重新查询获取自动生成的 ID
        testReader = readerMapper.findByReaderId("BL001");
    }

    @Test
    @DisplayName("完整黑名单流程：添加 → 判断 → 移出")
    void fullBlacklistFlow() {
        // 1. 初始状态：不在黑名单
        assertFalse(blacklistService.isBlacklisted(testReader.getId()));

        // 2. 添加到黑名单
        Blacklist blacklist = new Blacklist();
        blacklist.setReaderId(testReader.getId());
        blacklist.setReason("测试违规");

        Blacklist created = blacklistService.addToBlacklist(blacklist);

        assertNotNull(created);
        assertEquals(1, created.getBlacklisted());

        // 3. 验证在黑名单中
        assertTrue(blacklistService.isBlacklisted(testReader.getId()));

        // 4. 验证读者被禁用
        Reader disabledReader = readerMapper.findById(testReader.getId());
        assertEquals(0, disabledReader.getStatus());

        // 5. 移出黑名单
        blacklistService.removeFromBlacklist(created.getId());

        // 6. 验证不在黑名单
        assertFalse(blacklistService.isBlacklisted(testReader.getId()));

        // 7. 验证读者恢复
        Reader restoredReader = readerMapper.findById(testReader.getId());
        assertEquals(1, restoredReader.getStatus());
    }

    @Test
    @DisplayName("添加黑名单失败：读者不存在")
    void addToBlacklist_ReaderNotFound() {
        Blacklist blacklist = new Blacklist();
        blacklist.setReaderId(99999);

        assertThrows(BusinessException.class,
                () -> blacklistService.addToBlacklist(blacklist));
    }

    @Test
    @DisplayName("添加黑名单失败：已在黑名单中")
    void addToBlacklist_AlreadyBlacklisted() {
        // 先添加
        Blacklist blacklist = new Blacklist();
        blacklist.setReaderId(testReader.getId());
        blacklist.setReason("第一次违规");
        blacklistService.addToBlacklist(blacklist);

        // 再添加应失败
        Blacklist blacklist2 = new Blacklist();
        blacklist2.setReaderId(testReader.getId());
        blacklist2.setReason("第二次违规");

        assertThrows(BusinessException.class,
                () -> blacklistService.addToBlacklist(blacklist2));
    }

    @Test
    @DisplayName("移出黑名单失败：记录不存在")
    void removeFromBlacklist_NotFound() {
        assertThrows(BusinessException.class,
                () -> blacklistService.removeFromBlacklist(99999));
    }

    @Test
    @DisplayName("查询所有黑名单")
    void getAllBlacklist() {
        // 添加一条
        Blacklist blacklist = new Blacklist();
        blacklist.setReaderId(testReader.getId());
        blacklist.setReason("测试");
        blacklistService.addToBlacklist(blacklist);

        List<Blacklist> all = blacklistService.getAllBlacklist();

        assertNotNull(all);
        assertFalse(all.isEmpty());
    }

    @Test
    @DisplayName("查询活跃黑名单")
    void getActiveBlacklist() {
        Blacklist blacklist = new Blacklist();
        blacklist.setReaderId(testReader.getId());
        blacklist.setReason("测试");
        blacklistService.addToBlacklist(blacklist);

        List<Blacklist> active = blacklistService.getActiveBlacklist();

        assertNotNull(active);
        assertFalse(active.isEmpty());
    }

    @Test
    @DisplayName("违规累计达到阈值自动加入黑名单")
    void incrementViolation_AutoBlacklist() {
        // 前置：创建一条违规记录
        Blacklist blacklist = new Blacklist();
        blacklist.setReaderId(testReader.getId());
        blacklist.setViolationCount(0);
        blacklist.setBlacklisted(0);
        blacklistService.addToBlacklist(blacklist);

        // 连续违规3次（默认阈值）
        blacklistService.incrementViolation(testReader.getId());
        blacklistService.incrementViolation(testReader.getId());
        blacklistService.incrementViolation(testReader.getId());

        // 验证自动加入黑名单
        assertTrue(blacklistService.isBlacklisted(testReader.getId()));
    }

    @Test
    @DisplayName("查询黑名单记录详情")
    void getBlacklistById() {
        Blacklist blacklist = new Blacklist();
        blacklist.setReaderId(testReader.getId());
        blacklist.setReason("测试原因");

        Blacklist created = blacklistService.addToBlacklist(blacklist);

        Blacklist found = blacklistService.getBlacklistById(created.getId());

        assertNotNull(found);
        assertEquals(testReader.getId(), found.getReaderId());
        assertEquals("测试原因", found.getReason());
    }
}

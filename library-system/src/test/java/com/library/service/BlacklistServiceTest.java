package com.library.service;

import com.library.entity.Blacklist;
import com.library.entity.Reader;
import com.library.exception.BusinessException;
import com.library.mapper.BlacklistMapper;
import com.library.mapper.ReaderMapper;
import com.library.service.impl.BlacklistServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BlacklistService 单元测试")
class BlacklistServiceTest {

    @Mock
    private BlacklistMapper blacklistMapper;

    @Mock
    private ReaderMapper readerMapper;

    @Mock
    private SystemConfigService systemConfigService;

    @InjectMocks
    private BlacklistServiceImpl blacklistService;

    private Reader testReader;

    @BeforeEach
    void setUp() {
        testReader = new Reader();
        testReader.setId(1);
        testReader.setReaderId("2024001");
        testReader.setStatus(1);
    }

    // ==================== 查询测试 ====================

    @Test
    @DisplayName("查询所有黑名单")
    void getAllBlacklist() {
        when(blacklistMapper.findAll()).thenReturn(Collections.emptyList());

        List<Blacklist> result = blacklistService.getAllBlacklist();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("查询黑名单 - 记录不存在")
    void getBlacklistById_NotFound() {
        when(blacklistMapper.findById(999)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> blacklistService.getBlacklistById(999));
        assertEquals("黑名单记录不存在", ex.getMessage());
    }

    @Test
    @DisplayName("查询黑名单 - 记录存在")
    void getBlacklistById_Found() {
        Blacklist blacklist = new Blacklist();
        blacklist.setId(1);
        blacklist.setReaderId(1);
        when(blacklistMapper.findById(1)).thenReturn(blacklist);

        Blacklist result = blacklistService.getBlacklistById(1);

        assertEquals(1, result.getId());
    }

    // ==================== 是否在黑名单测试 ====================

    @Test
    @DisplayName("判断是否在黑名单 - 无记录")
    void isBlacklisted_NoRecord() {
        when(blacklistMapper.findByReaderId(1)).thenReturn(null);

        assertFalse(blacklistService.isBlacklisted(1));
    }

    @Test
    @DisplayName("判断是否在黑名单 - 未被拉黑")
    void isBlacklisted_NotBlacklisted() {
        Blacklist blacklist = new Blacklist();
        blacklist.setReaderId(1);
        blacklist.setBlacklisted(0);
        when(blacklistMapper.findByReaderId(1)).thenReturn(blacklist);

        assertFalse(blacklistService.isBlacklisted(1));
    }

    @Test
    @DisplayName("判断是否在黑名单 - 已被拉黑且未过期")
    void isBlacklisted_BlacklistedAndActive() {
        Blacklist blacklist = new Blacklist();
        blacklist.setReaderId(1);
        blacklist.setBlacklisted(1);
        blacklist.setEndTime(LocalDateTime.now().plusDays(10));
        when(blacklistMapper.findByReaderId(1)).thenReturn(blacklist);

        assertTrue(blacklistService.isBlacklisted(1));
    }

    @Test
    @DisplayName("判断是否在黑名单 - 已被拉黑但已过期")
    void isBlacklisted_BlacklistedButExpired() {
        Blacklist blacklist = new Blacklist();
        blacklist.setReaderId(1);
        blacklist.setBlacklisted(1);
        blacklist.setEndTime(LocalDateTime.now().minusDays(1));
        when(blacklistMapper.findByReaderId(1)).thenReturn(blacklist);

        assertFalse(blacklistService.isBlacklisted(1));
    }

    // ==================== 添加黑名单测试 ====================

    @Test
    @DisplayName("添加黑名单 - 正常添加")
    void addToBlacklist_Success() {
        Blacklist blacklist = new Blacklist();
        blacklist.setReaderId(1);
        blacklist.setReason("测试原因");

        when(readerMapper.findById(1)).thenReturn(testReader);
        when(blacklistMapper.findByReaderId(1)).thenReturn(null);
        when(blacklistMapper.insert(any(Blacklist.class))).thenReturn(1);

        Blacklist result = blacklistService.addToBlacklist(blacklist);

        assertEquals(1, result.getBlacklisted());
        verify(readerMapper).updateStatus(1, 0); // 禁用读者
    }

    @Test
    @DisplayName("添加黑名单 - 读者不存在")
    void addToBlacklist_ReaderNotFound() {
        Blacklist blacklist = new Blacklist();
        blacklist.setReaderId(999);

        when(readerMapper.findById(999)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> blacklistService.addToBlacklist(blacklist));
        assertEquals("读者不存在", ex.getMessage());
    }

    @Test
    @DisplayName("添加黑名单 - 已在黑名单中")
    void addToBlacklist_AlreadyBlacklisted() {
        Blacklist existing = new Blacklist();
        existing.setReaderId(1);
        existing.setBlacklisted(1);
        existing.setEndTime(LocalDateTime.now().plusDays(10));

        Blacklist blacklist = new Blacklist();
        blacklist.setReaderId(1);

        when(readerMapper.findById(1)).thenReturn(testReader);
        when(blacklistMapper.findByReaderId(1)).thenReturn(existing);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> blacklistService.addToBlacklist(blacklist));
        assertEquals("该读者已在黑名单中", ex.getMessage());
    }

    // ==================== 移出黑名单测试 ====================

    @Test
    @DisplayName("移出黑名单 - 正常移出")
    void removeFromBlacklist_Success() {
        Blacklist blacklist = new Blacklist();
        blacklist.setId(1);
        blacklist.setReaderId(1);

        when(blacklistMapper.findById(1)).thenReturn(blacklist);
        when(readerMapper.findById(1)).thenReturn(testReader);

        blacklistService.removeFromBlacklist(1);

        assertEquals(0, blacklist.getBlacklisted());
        verify(blacklistMapper).update(blacklist);
        verify(readerMapper).updateStatus(1, 1); // 恢复读者
    }

    @Test
    @DisplayName("移出黑名单 - 记录不存在")
    void removeFromBlacklist_NotFound() {
        when(blacklistMapper.findById(999)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> blacklistService.removeFromBlacklist(999));
        assertEquals("黑名单记录不存在", ex.getMessage());
    }

    // ==================== 违规累计测试 ====================

    @Test
    @DisplayName("违规累计 - 未达阈值")
    void incrementViolation_BelowThreshold() {
        when(systemConfigService.getConfigValue("library.blacklist.violation-threshold")).thenReturn("3");

        // mock 返回递增后的记录（violationCount=2，未达阈值3）
        Blacklist existingAfterIncrement = new Blacklist();
        existingAfterIncrement.setReaderId(1);
        existingAfterIncrement.setViolationCount(2); // 递增后未达阈值
        existingAfterIncrement.setBlacklisted(0);

        when(blacklistMapper.findByReaderId(1)).thenReturn(existingAfterIncrement);

        blacklistService.incrementViolation(1);

        verify(blacklistMapper).incrementViolationCount(1);
        // 未达阈值，不应自动加入黑名单
        verify(blacklistMapper, never()).insert(any(Blacklist.class));
    }

    @Test
    @DisplayName("违规累计 - 达到阈值自动加入黑名单")
    void incrementViolation_ReachesThreshold() {
        when(systemConfigService.getConfigValue("library.blacklist.violation-threshold")).thenReturn("3");

        // incrementViolation 先调 incrementViolationCount，再调 findByReaderId
        // mock 返回递增后的记录（violationCount=3，达到阈值）
        Blacklist existingAfterIncrement = new Blacklist();
        existingAfterIncrement.setReaderId(1);
        existingAfterIncrement.setViolationCount(3); // 递增后达到阈值
        existingAfterIncrement.setBlacklisted(0);

        when(blacklistMapper.findByReaderId(1)).thenReturn(existingAfterIncrement);
        when(readerMapper.findById(1)).thenReturn(testReader);

        blacklistService.incrementViolation(1);

        verify(blacklistMapper).incrementViolationCount(1);
        // 达到阈值，应自动加入黑名单
        verify(blacklistMapper).insert(any(Blacklist.class));
        verify(readerMapper).updateStatus(1, 0); // 禁用读者
    }

    // ==================== 查询活跃黑名单测试 ====================

    @Test
    @DisplayName("查询活跃黑名单")
    void getActiveBlacklist() {
        when(blacklistMapper.findActiveBlacklist()).thenReturn(Collections.emptyList());

        List<Blacklist> result = blacklistService.getActiveBlacklist();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

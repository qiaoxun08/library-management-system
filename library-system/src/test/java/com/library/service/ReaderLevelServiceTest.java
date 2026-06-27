package com.library.service;

import com.library.entity.ReaderLevel;
import com.library.mapper.ReaderLevelMapper;
import com.library.service.impl.ReaderLevelServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReaderLevelService 单元测试")
class ReaderLevelServiceTest {

    @Mock
    private ReaderLevelMapper readerLevelMapper;

    @InjectMocks
    private ReaderLevelServiceImpl readerLevelService;

    private ReaderLevel testLevel;

    @BeforeEach
    void setUp() {
        testLevel = new ReaderLevel();
        testLevel.setReaderId(1);
        testLevel.setPoints(0);
        testLevel.setLevel("普通");
        testLevel.setTotalBorrowCount(0);
        testLevel.setTotalReturnOnTime(0);
    }

    // ==================== 初始化测试 ====================

    @Test
    @DisplayName("初始化读者等级 - 首次初始化")
    void initReaderLevel_FirstTime() {
        when(readerLevelMapper.findByReaderId(1)).thenReturn(null);
        when(readerLevelMapper.insert(any(ReaderLevel.class))).thenReturn(1);

        readerLevelService.initReaderLevel(1);

        verify(readerLevelMapper).insert(any(ReaderLevel.class));
    }

    @Test
    @DisplayName("初始化读者等级 - 已存在不重复初始化")
    void initReaderLevel_AlreadyExists() {
        when(readerLevelMapper.findByReaderId(1)).thenReturn(testLevel);

        readerLevelService.initReaderLevel(1);

        verify(readerLevelMapper, never()).insert(any(ReaderLevel.class));
    }

    // ==================== 积分测试 ====================

    @Test
    @DisplayName("增加积分")
    void addPoints() {
        when(readerLevelMapper.addPoints(1, 10)).thenReturn(1);
        when(readerLevelMapper.findByReaderId(1)).thenReturn(testLevel);

        readerLevelService.addPoints(1, 10);

        verify(readerLevelMapper).addPoints(1, 10);
        verify(readerLevelMapper).update(any(ReaderLevel.class));
    }

    @Test
    @DisplayName("扣除积分")
    void subtractPoints() {
        testLevel.setPoints(100);
        when(readerLevelMapper.subtractPoints(1, 20)).thenReturn(1);
        when(readerLevelMapper.findByReaderId(1)).thenReturn(testLevel);

        readerLevelService.subtractPoints(1, 20);

        verify(readerLevelMapper).subtractPoints(1, 20);
    }

    // ==================== 等级升级测试 ====================

    @Test
    @DisplayName("等级升级 - 普通（0-199分）")
    void updateLevel_Normal() {
        testLevel.setPoints(50);
        when(readerLevelMapper.findByReaderId(1)).thenReturn(testLevel);

        readerLevelService.updateLevel(1);

        assertEquals("普通", testLevel.getLevel());
        verify(readerLevelMapper).update(testLevel);
    }

    @Test
    @DisplayName("等级升级 - 银牌（200-499分）")
    void updateLevel_Silver() {
        testLevel.setPoints(250);
        when(readerLevelMapper.findByReaderId(1)).thenReturn(testLevel);

        readerLevelService.updateLevel(1);

        assertEquals("银牌", testLevel.getLevel());
    }

    @Test
    @DisplayName("等级升级 - 金牌（500-999分）")
    void updateLevel_Gold() {
        testLevel.setPoints(600);
        when(readerLevelMapper.findByReaderId(1)).thenReturn(testLevel);

        readerLevelService.updateLevel(1);

        assertEquals("金牌", testLevel.getLevel());
    }

    @Test
    @DisplayName("等级升级 - 钻石（>=1000分）")
    void updateLevel_Diamond() {
        testLevel.setPoints(1200);
        when(readerLevelMapper.findByReaderId(1)).thenReturn(testLevel);

        readerLevelService.updateLevel(1);

        assertEquals("钻石", testLevel.getLevel());
    }

    @Test
    @DisplayName("等级升级 - 边界值200分升银牌")
    void updateLevel_BoundarySilver() {
        testLevel.setPoints(200);
        when(readerLevelMapper.findByReaderId(1)).thenReturn(testLevel);

        readerLevelService.updateLevel(1);

        assertEquals("银牌", testLevel.getLevel());
    }

    @Test
    @DisplayName("等级升级 - 边界值500分升金牌")
    void updateLevel_BoundaryGold() {
        testLevel.setPoints(500);
        when(readerLevelMapper.findByReaderId(1)).thenReturn(testLevel);

        readerLevelService.updateLevel(1);

        assertEquals("金牌", testLevel.getLevel());
    }

    @Test
    @DisplayName("等级升级 - 边界值1000分升钻石")
    void updateLevel_BoundaryDiamond() {
        testLevel.setPoints(1000);
        when(readerLevelMapper.findByReaderId(1)).thenReturn(testLevel);

        readerLevelService.updateLevel(1);

        assertEquals("钻石", testLevel.getLevel());
    }

    @Test
    @DisplayName("等级更新 - 读者等级记录不存在")
    void updateLevel_NotFound() {
        when(readerLevelMapper.findByReaderId(999)).thenReturn(null);

        readerLevelService.updateLevel(999);

        verify(readerLevelMapper, never()).update(any(ReaderLevel.class));
    }

    // ==================== 查询测试 ====================

    @Test
    @DisplayName("获取读者等级")
    void getReaderLevel() {
        when(readerLevelMapper.findByReaderId(1)).thenReturn(testLevel);

        ReaderLevel result = readerLevelService.getReaderLevel(1);

        assertNotNull(result);
        assertEquals("普通", result.getLevel());
    }

    @Test
    @DisplayName("获取所有读者等级")
    void getAllReaderLevels() {
        when(readerLevelMapper.findAll()).thenReturn(Collections.emptyList());

        List<ReaderLevel> result = readerLevelService.getAllReaderLevels();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== 借阅计数测试 ====================

    @Test
    @DisplayName("增加借阅次数")
    void incrementBorrowCount() {
        readerLevelService.incrementBorrowCount(1);

        verify(readerLevelMapper).incrementBorrowCount(1);
    }

    @Test
    @DisplayName("增加按时归还次数")
    void incrementReturnOnTime() {
        readerLevelService.incrementReturnOnTime(1);

        verify(readerLevelMapper).incrementReturnOnTime(1);
    }
}

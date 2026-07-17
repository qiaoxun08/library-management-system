package com.library.service;

import com.library.dto.StatisticsDTO;
import java.util.List;
import java.util.Map;

public interface StatisticsService {
    StatisticsDTO getDashboardStatistics();
    Map<String, Integer> getBooksByCategory();
    List<Integer> getBorrowingsByMonth();
    Map<Integer, Integer> getReservationsByStatus();
    int getActiveReaderCount();

    /**
     * 获取实时统计数据（数据大屏使用）
     * 包含：在线人数、今日借阅量按小时分布、热门图书TOP10
     */
    Map<String, Object> getRealtimeStatistics();
}

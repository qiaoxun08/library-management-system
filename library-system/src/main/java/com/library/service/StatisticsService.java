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
}

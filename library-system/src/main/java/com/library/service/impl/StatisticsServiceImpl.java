package com.library.service.impl;

import com.library.dto.BookCategoryCount;
import com.library.dto.ReservationStatusCount;
import com.library.dto.StatisticsDTO;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowingMapper;
import com.library.mapper.ReaderMapper;
import com.library.mapper.ReservationMapper;
import com.library.mapper.SeatMapper;
import com.library.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private ReaderMapper readerMapper;

    @Autowired
    private BorrowingMapper borrowingMapper;

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private SeatMapper seatMapper;

    @Override
    public StatisticsDTO getDashboardStatistics() {
        StatisticsDTO dto = new StatisticsDTO();

        // Book statistics
        dto.setTotalBooks(bookMapper.countTotalBooks());
        dto.setAvailableBooks(bookMapper.countAvailableBooks());

        // Reader statistics
        dto.setTotalReaders(readerMapper.countTotalReaders());
        dto.setActiveReaders(readerMapper.countActiveReaders());

        // Borrowing statistics
        dto.setTodayBorrowings(borrowingMapper.countTodayBorrowings());
        dto.setTotalBorrowings(borrowingMapper.countActiveBorrowings());
        dto.setOverdueBooks(borrowingMapper.countOverdueBooks());

        // Reservation statistics
        dto.setTodayReservations(reservationMapper.countTodayReservations());

        // Seat statistics
        dto.setTotalSeats(seatMapper.countTotalSeats());
        dto.setAvailableSeats(seatMapper.countAvailableSeats());

        // Chart data - convert list to map
        List<BookCategoryCount> categoryCounts = bookMapper.countByCategory();
        Map<String, Integer> booksByCategory = new HashMap<>();
        for (BookCategoryCount count : categoryCounts) {
            booksByCategory.put(count.getCategory(), count.getCount());
        }
        dto.setBooksByCategory(booksByCategory);
        dto.setBorrowingsByMonth(borrowingMapper.countByMonth());
        // Convert reservation status list to map
        List<ReservationStatusCount> statusCounts = reservationMapper.countByStatus();
        Map<Integer, Integer> reservationsByStatus = new HashMap<>();
        for (ReservationStatusCount count : statusCounts) {
            reservationsByStatus.put(count.getStatus(), count.getCount());
        }
        dto.setReservationsByStatus(reservationsByStatus);

        return dto;
    }

    @Override
    public Map<String, Integer> getBooksByCategory() {
        List<BookCategoryCount> categoryCounts = bookMapper.countByCategory();
        Map<String, Integer> booksByCategory = new HashMap<>();
        for (BookCategoryCount count : categoryCounts) {
            booksByCategory.put(count.getCategory(), count.getCount());
        }
        return booksByCategory;
    }

    @Override
    public List<Integer> getBorrowingsByMonth() {
        return borrowingMapper.countByMonth();
    }

    @Override
    public Map<Integer, Integer> getReservationsByStatus() {
        List<ReservationStatusCount> statusCounts = reservationMapper.countByStatus();
        Map<Integer, Integer> reservationsByStatus = new HashMap<>();
        for (ReservationStatusCount count : statusCounts) {
            reservationsByStatus.put(count.getStatus(), count.getCount());
        }
        return reservationsByStatus;
    }

    @Override
    public int getActiveReaderCount() {
        return readerMapper.countActiveReaders();
    }

    @Override
    public Map<String, Object> getRealtimeStatistics() {
        Map<String, Object> result = new HashMap<>();

        // 在线人数（活跃读者数）
        result.put("onlineCount", readerMapper.countActiveReaders());

        // 今日借阅量按小时分布
        List<Map<String, Object>> todayBorrowingsByHour = borrowingMapper.countTodayByHour();
        result.put("todayBorrowingsByHour", todayBorrowingsByHour);

        // 热门图书TOP10
        List<Map<String, Object>> hotBooks = bookMapper.findHotBooksTop10();
        result.put("hotBooks", hotBooks);

        // 座位占用率热力图数据
        Map<String, Object> seatHeatmap = seatMapper.getSeatHeatmapData();
        result.put("seatHeatmap", seatHeatmap);

        // 借阅分类分布
        List<Map<String, Object>> categoryDistribution = borrowingMapper.countByCategoryDistribution();
        result.put("categoryDistribution", categoryDistribution);

        // 本月逾期率趋势（按天）
        List<Map<String, Object>> overdueTrend = borrowingMapper.countOverdueTrendByDay();
        result.put("overdueTrend", overdueTrend);

        return result;
    }
}

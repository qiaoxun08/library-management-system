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
}

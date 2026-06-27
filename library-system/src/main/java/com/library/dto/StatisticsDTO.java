package com.library.dto;

import java.util.List;
import java.util.Map;

public class StatisticsDTO {
    private Integer totalBooks;
    private Integer availableBooks;
    private Integer totalReaders;
    private Integer activeReaders;
    private Integer todayBorrowings;
    private Integer todayReservations;
    private Integer totalSeats;
    private Integer availableSeats;
    private Integer totalBorrowings;
    private Integer overdueBooks;
    private Map<String, Integer> booksByCategory;
    private List<Integer> borrowingsByMonth;
    private Map<Integer, Integer> reservationsByStatus;

    public Integer getTotalBooks() { return totalBooks; }
    public void setTotalBooks(Integer totalBooks) { this.totalBooks = totalBooks; }

    public Integer getAvailableBooks() { return availableBooks; }
    public void setAvailableBooks(Integer availableBooks) { this.availableBooks = availableBooks; }

    public Integer getTotalReaders() { return totalReaders; }
    public void setTotalReaders(Integer totalReaders) { this.totalReaders = totalReaders; }

    public Integer getActiveReaders() { return activeReaders; }
    public void setActiveReaders(Integer activeReaders) { this.activeReaders = activeReaders; }

    public Integer getTodayBorrowings() { return todayBorrowings; }
    public void setTodayBorrowings(Integer todayBorrowings) { this.todayBorrowings = todayBorrowings; }

    public Integer getTodayReservations() { return todayReservations; }
    public void setTodayReservations(Integer todayReservations) { this.todayReservations = todayReservations; }

    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }

    public Integer getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }

    public Integer getTotalBorrowings() { return totalBorrowings; }
    public void setTotalBorrowings(Integer totalBorrowings) { this.totalBorrowings = totalBorrowings; }

    public Integer getOverdueBooks() { return overdueBooks; }
    public void setOverdueBooks(Integer overdueBooks) { this.overdueBooks = overdueBooks; }

    public Map<String, Integer> getBooksByCategory() { return booksByCategory; }
    public void setBooksByCategory(Map<String, Integer> booksByCategory) { this.booksByCategory = booksByCategory; }

    public List<Integer> getBorrowingsByMonth() { return borrowingsByMonth; }
    public void setBorrowingsByMonth(List<Integer> borrowingsByMonth) { this.borrowingsByMonth = borrowingsByMonth; }

    public Map<Integer, Integer> getReservationsByStatus() { return reservationsByStatus; }
    public void setReservationsByStatus(Map<Integer, Integer> reservationsByStatus) { this.reservationsByStatus = reservationsByStatus; }
}

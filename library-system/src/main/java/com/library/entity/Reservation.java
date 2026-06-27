package com.library.entity;

import java.time.LocalDateTime;

public class Reservation {
    private Integer id;
    private Integer readerId;
    private Integer bookId;
    private Integer seatId;
    private LocalDateTime reservationDate;
    private Integer status; // 0-待审批 1-已批准 2-已拒绝 3-已取消 4-已过期
    private String preferredTimeSlot;
    private LocalDateTime expiryDate;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getReaderId() { return readerId; }
    public void setReaderId(Integer readerId) { this.readerId = readerId; }
    public Integer getBookId() { return bookId; }
    public void setBookId(Integer bookId) { this.bookId = bookId; }
    public Integer getSeatId() { return seatId; }
    public void setSeatId(Integer seatId) { this.seatId = seatId; }
    public LocalDateTime getReservationDate() { return reservationDate; }
    public void setReservationDate(LocalDateTime reservationDate) { this.reservationDate = reservationDate; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getPreferredTimeSlot() { return preferredTimeSlot; }
    public void setPreferredTimeSlot(String preferredTimeSlot) { this.preferredTimeSlot = preferredTimeSlot; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
}

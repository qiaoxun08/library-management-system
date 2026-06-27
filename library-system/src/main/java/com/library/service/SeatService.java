package com.library.service;

import com.library.entity.Seat;
import java.util.List;

public interface SeatService {
    List<Seat> getAllSeats();
    Seat getSeatById(Integer id);
    Seat getSeatBySeatNumber(String seatNumber);
    List<Seat> getSeatsByArea(String area);
    List<Seat> getSeatsByStatus(Integer status);
    Seat addSeat(Seat seat);
    Seat updateSeat(Seat seat);
    void deleteSeat(Integer id);
    void updateSeatStatus(Integer id, Integer status);
}

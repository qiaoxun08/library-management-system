package com.library.service;

import com.library.entity.Seat;

import java.util.List;
import java.util.Map;

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

    /**
     * 获取座位时间轴视图数据
     * @param date 日期，格式 yyyy-MM-dd
     * @return 每个座位在各时段的占用状态
     */
    List<Map<String, Object>> getSeatTimeline(String date);
}

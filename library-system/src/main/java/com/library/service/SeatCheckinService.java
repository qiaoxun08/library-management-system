package com.library.service;

import com.library.entity.SeatCheckin;

import java.util.List;

/**
 * 座位签到服务接口
 */
public interface SeatCheckinService {
    List<SeatCheckin> getAllCheckins();
    SeatCheckin getCheckinById(Integer id);
    List<SeatCheckin> getCheckinsByReaderId(Integer readerId);
    SeatCheckin checkin(Integer seatId, Integer readerId, Integer reservationId);
    SeatCheckin checkout(Integer checkinId);
}

package com.library.service.impl;

import com.library.entity.Seat;
import com.library.mapper.SeatMapper;
import com.library.service.SeatService;
import com.library.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeatServiceImpl implements SeatService {

    @Autowired
    private SeatMapper seatMapper;

    @Override
    public List<Seat> getAllSeats() {
        return seatMapper.findAll();
    }

    @Override
    public Seat getSeatById(Integer id) {
        return seatMapper.findById(id);
    }

    @Override
    public Seat getSeatBySeatNumber(String seatNumber) {
        return seatMapper.findBySeatNumber(seatNumber);
    }

    @Override
    public List<Seat> getSeatsByArea(String area) {
        return seatMapper.findByArea(area);
    }

    @Override
    public List<Seat> getSeatsByStatus(Integer status) {
        return seatMapper.findByStatus(status);
    }

    @Override
    public Seat addSeat(Seat seat) {
        seatMapper.insert(seat);
        return seat;
    }

    @Override
    public Seat updateSeat(Seat seat) {
        seatMapper.update(seat);
        return seat;
    }

    @Override
    public void deleteSeat(Integer id) {
        seatMapper.delete(id);
    }

    @Override
    public void updateSeatStatus(Integer id, Integer newStatus) {
        Seat seat = seatMapper.findById(id);
        if (seat == null) {
            throw new BusinessException("座位不存在");
        }
        Integer currentStatus = seat.getStatus();
        validateStatusTransition(currentStatus, newStatus);
        seatMapper.updateStatus(id, newStatus);
    }

    private void validateStatusTransition(Integer current, Integer target) {
        // 0-空闲 1-使用中 2-已预约
        boolean valid = switch (current) {
            case 0 -> target == 0 || target == 1 || target == 2; // 空闲 -> 空闲(幂等)/使用中/已预约
            case 1 -> target == 0;                 // 使用中 -> 空闲（释放）
            case 2 -> target == 0 || target == 1;  // 已预约 -> 空闲（取消）/使用中（签到）
            default -> false;
        };
        if (!valid) {
            throw new BusinessException("座位状态转换不合法: " + getStatusName(current) + " -> " + getStatusName(target));
        }
    }

    private String getStatusName(Integer status) {
        return switch (status) {
            case 0 -> "空闲";
            case 1 -> "使用中";
            case 2 -> "已预约";
            default -> "未知";
        };
    }
}

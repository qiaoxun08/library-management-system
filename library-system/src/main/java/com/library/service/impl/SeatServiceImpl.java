package com.library.service.impl;

import com.library.entity.Seat;
import com.library.entity.Scheduling;
import com.library.mapper.SeatMapper;
import com.library.mapper.SchedulingMapper;
import com.library.service.SeatService;
import com.library.service.RedisCacheService;
import com.library.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SeatServiceImpl implements SeatService {

    private static final Logger log = LoggerFactory.getLogger(SeatServiceImpl.class);
    private static final String SEAT_CACHE_PREFIX = "seat:detail:";
    private static final String SEAT_AREA_CACHE_PREFIX = "seat:area:";

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private SchedulingMapper schedulingMapper;

    @Autowired
    private RedisCacheService redisCacheService;

    @Override
    public List<Seat> getAllSeats() {
        return seatMapper.findAll();
    }

    @Override
    public Seat getSeatById(Integer id) {
        String cacheKey = SEAT_CACHE_PREFIX + id;
        Seat cached = redisCacheService.get(cacheKey, Seat.class);
        if (cached != null) {
            return cached;
        }
        Seat seat = seatMapper.findById(id);
        if (seat != null) {
            redisCacheService.set(cacheKey, seat, 30);
        }
        return seat;
    }

    @Override
    public Seat getSeatBySeatNumber(String seatNumber) {
        return seatMapper.findBySeatNumber(seatNumber);
    }

    @Override
    public List<Seat> getSeatsByArea(String area) {
        String cacheKey = SEAT_AREA_CACHE_PREFIX + area;
        @SuppressWarnings("unchecked")
        List<Seat> cached = redisCacheService.get(cacheKey, List.class);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        List<Seat> seats = seatMapper.findByArea(area);
        if (seats != null && !seats.isEmpty()) {
            redisCacheService.set(cacheKey, seats, 10);
        }
        return seats;
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
        // 清除缓存
        redisCacheService.delete(SEAT_CACHE_PREFIX + id);
        if (seat.getArea() != null) {
            redisCacheService.delete(SEAT_AREA_CACHE_PREFIX + seat.getArea());
        }
        log.debug("座位状态更新: id={}, {} -> {}", id, currentStatus, newStatus);
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

    @Override
    public List<Map<String, Object>> getSeatTimeline(String date) {
        // 1. 查询所有座位
        List<Seat> seats = seatMapper.findAll();
        // 2. 查询指定日期的所有排期记录
        List<Scheduling> schedulings = schedulingMapper.findByDate(date);

        // 3. 构建座位 x 时段矩阵（8:00 ~ 22:00，共14个时段）
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate queryDate = LocalDate.parse(date);
        LocalDateTime now = LocalDateTime.now();

        for (Seat seat : seats) {
            Map<String, Object> seatData = new LinkedHashMap<>();
            seatData.put("seatId", seat.getId());
            seatData.put("seatNumber", seat.getSeatNumber());
            seatData.put("area", seat.getArea());

            // 构建每个时段的状态
            List<Map<String, Object>> timeSlots = new ArrayList<>();
            for (int hour = 8; hour <= 21; hour++) {
                Map<String, Object> slot = new LinkedHashMap<>();
                slot.put("hour", hour);

                // 判断该时段是否已过
                LocalDateTime slotTime = queryDate.atTime(hour, 0);
                if (slotTime.isBefore(now)) {
                    slot.put("status", "past");
                } else {
                    // 查找该座位在该时段是否有排期记录
                    String slotStatus = "free";
                    for (Scheduling s : schedulings) {
                        if (!s.getSeatId().equals(seat.getId())) {
                            continue;
                        }
                        LocalDateTime start = s.getStartTime();
                        LocalDateTime end = s.getEndTime();
                        // 排期覆盖该时段：start <= hour:00 < end
                        if (!start.isAfter(slotTime) && end.isAfter(slotTime)) {
                            // status: 0-待确认/已预约, 1-使用中, 2-已完成/已取消
                            if (s.getStatus() != null && s.getStatus() == 1) {
                                slotStatus = "occupied";
                                break;
                            } else if (s.getStatus() != null && (s.getStatus() == 0 || s.getStatus() == 2)) {
                                slotStatus = "reserved";
                                // 不break，继续查找是否有occupied的覆盖
                            }
                        }
                    }
                    slot.put("status", slotStatus);
                }
                timeSlots.add(slot);
            }

            seatData.put("timeSlots", timeSlots);
            result.add(seatData);
        }

        return result;
    }
}

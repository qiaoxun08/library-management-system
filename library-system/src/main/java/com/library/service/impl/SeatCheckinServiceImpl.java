package com.library.service.impl;

import com.library.dto.ReservationDTO;
import com.library.entity.SeatCheckin;
import com.library.exception.BusinessException;
import com.library.mapper.SeatCheckinMapper;
import com.library.mapper.SeatMapper;
import com.library.mapper.ReservationMapper;
import com.library.entity.Seat;
import com.library.service.SeatCheckinService;
import com.library.service.BlacklistService;
import com.library.service.SystemConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 座位签到服务实现
 */
@Service
public class SeatCheckinServiceImpl implements SeatCheckinService {

    private static final Logger log = LoggerFactory.getLogger(SeatCheckinServiceImpl.class);

    @Autowired
    private SeatCheckinMapper seatCheckinMapper;

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private BlacklistService blacklistService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Override
    public List<SeatCheckin> getAllCheckins() {
        return seatCheckinMapper.findAll();
    }

    @Override
    public SeatCheckin getCheckinById(Integer id) {
        SeatCheckin checkin = seatCheckinMapper.findById(id);
        if (checkin == null) {
            throw new BusinessException("签到记录不存在");
        }
        return checkin;
    }

    @Override
    public List<SeatCheckin> getCheckinsByReaderId(Integer readerId) {
        return seatCheckinMapper.findByReaderId(readerId);
    }

    @Override
    @Transactional
    public SeatCheckin checkin(Integer seatId, Integer readerId, Integer reservationId) {
        // 检查座位是否存在
        Seat seat = seatMapper.findById(seatId);
        if (seat == null) {
            throw new BusinessException("座位不存在");
        }

        // 检查读者是否已有在用座位
        SeatCheckin activeCheckin = seatCheckinMapper.findActiveByReaderId(readerId);
        if (activeCheckin != null) {
            throw new BusinessException("您已有在用座位，请先签退");
        }

        // 检查座位是否已被占用
        SeatCheckin activeSeat = seatCheckinMapper.findActiveBySeatId(seatId);
        if (activeSeat != null) {
            throw new BusinessException("该座位已被占用");
        }

        // 创建签到记录
        SeatCheckin checkin = new SeatCheckin();
        checkin.setSeatId(seatId);
        checkin.setReaderId(readerId);
        checkin.setReservationId(reservationId);
        checkin.setStatus(1); // 1: 使用中
        seatCheckinMapper.insert(checkin);

        // 更新座位状态为使用中
        seat.setStatus(1);
        seatMapper.update(seat);

        return checkin;
    }

    @Override
    @Transactional
    public SeatCheckin checkout(Integer checkinId) {
        SeatCheckin checkin = seatCheckinMapper.findById(checkinId);
        if (checkin == null) {
            throw new BusinessException("签到记录不存在");
        }

        if (checkin.getStatus() != 1) {
            throw new BusinessException("该签到记录不是使用中状态");
        }

        // 更新签退时间
        checkin.setCheckoutTime(LocalDateTime.now());
        checkin.setStatus(0); // 0: 已签退
        seatCheckinMapper.update(checkin);

        // 更新座位状态为空闲
        Seat seat = seatMapper.findById(checkin.getSeatId());
        if (seat != null) {
            seat.setStatus(0);
            seatMapper.update(seat);
        }

        return checkin;
    }

    /**
     * 定时任务：每5分钟释放已批准但超过30分钟未签到的座位预约
     */
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void releaseUncheckinedReservations() {
        log.info("开始检查未签到的座位预约...");
        String minutesStr = systemConfigService.getConfigValue("library.seat.auto-release-minutes");
        int minutes = minutesStr != null ? Integer.parseInt(minutesStr) : 30;

        // 查询已批准(status=1)且有座位(seat_id不为空)且创建时间超过指定分钟的预约
        List<ReservationDTO> approvedReservations = reservationMapper.findByStatus(1);
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(minutes);

        for (ReservationDTO reservation : approvedReservations) {
            if (reservation.getSeatId() != null && reservation.getStartTime() != null
                    && reservation.getStartTime().isBefore(cutoffTime)) {
                // 检查是否已有签到记录
                SeatCheckin activeCheckin = seatCheckinMapper.findActiveBySeatId(reservation.getSeatId());
                if (activeCheckin == null) {
                    // 超时未签到，释放座位
                    reservationMapper.updateStatus(reservation.getId(), 4); // 4: 已过期
                    Seat seat = seatMapper.findById(reservation.getSeatId());
                    if (seat != null) {
                        seat.setStatus(0);
                        seatMapper.update(seat);
                    }
                    // 违约累计
                    blacklistService.incrementViolation(reservation.getReaderId());
                    log.info("释放超时未签到座位: reservationId={}, seatId={}, readerId={}",
                            reservation.getId(), reservation.getSeatId(), reservation.getReaderId());
                }
            }
        }
        log.info("未签到座位检查完成");
    }
}

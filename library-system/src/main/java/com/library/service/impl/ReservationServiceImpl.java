package com.library.service.impl;

import com.library.dto.ReservationDTO;
import com.library.entity.Reservation;
import com.library.mapper.ReservationMapper;
import com.library.mapper.ReaderMapper;
import com.library.service.ReservationService;
import com.library.service.SeatService;
import com.library.service.BorrowingService;
import com.library.service.BlacklistService;
import com.library.service.RedisLockService;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowingMapper;
import com.library.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationServiceImpl implements ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private ReaderMapper readerMapper;

    @Autowired
    private SeatService seatService;

    @Autowired
    private BorrowingService borrowingService;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private BorrowingMapper borrowingMapper;

    @Autowired
    private BlacklistService blacklistService;

    @Autowired
    private RedisLockService redisLockService;

    @Override
    public List<ReservationDTO> getAllReservations() {
        return reservationMapper.findAll();
    }

    @Override
    public ReservationDTO getReservationById(Integer id) {
        return reservationMapper.findById(id);
    }

    @Override
    public List<ReservationDTO> getReservationsByReaderId(Integer readerId) {
        return reservationMapper.findByReaderId(readerId);
    }

    @Override
    public List<ReservationDTO> getReservationsByReaderIdString(String readerId) {
        return reservationMapper.findByReaderIdString(readerId);
    }

    @Override
    public List<ReservationDTO> getReservationsByBookId(Integer bookId) {
        return reservationMapper.findByBookId(bookId);
    }

    @Override
    public List<ReservationDTO> getReservationsByStatus(Integer status) {
        return reservationMapper.findByStatus(status);
    }

    @Override
    public Reservation addReservation(Reservation reservation) {
        // 转换 readerId（学号）为数据库 ID
        if (reservation.getReaderId() != null) {
            var reader = readerMapper.findById(reservation.getReaderId());
            if (reader == null) {
                reader = readerMapper.findByReaderId(String.valueOf(reservation.getReaderId()));
            }
            if (reader != null) {
                reservation.setReaderId(reader.getId());
            }
        }

        // 检查黑名单
        if (reservation.getReaderId() != null && blacklistService.isBlacklisted(reservation.getReaderId())) {
            throw new BusinessException("您在黑名单中，无法预约");
        }

        // 座位预约：使用分布式锁防止并发抢座
        if (reservation.getSeatId() != null) {
            String lockKey = "seat:reserve:" + reservation.getSeatId();
            // 锁持有时间 5 秒，防止死锁
            return redisLockService.executeWithLock(lockKey, 5, () -> {
                // 重复预约检查：同一读者不能对同一座位有待审批或已批准的预约
                List<ReservationDTO> existing = reservationMapper.findBySeatId(reservation.getSeatId());
                boolean duplicate = existing.stream().anyMatch(r ->
                        r.getReaderId() != null && r.getReaderId().equals(reservation.getReaderId())
                        && (r.getStatus() == 0 || r.getStatus() == 1));
                if (duplicate) {
                    throw new BusinessException("您已预约过该座位，请等待审批或取消已有预约");
                }

                // 检查座位是否已被其他预约占用（同一时段）
                boolean taken = existing.stream().anyMatch(r ->
                        r.getStatus() == 0 || r.getStatus() == 1);
                if (taken) {
                    throw new BusinessException("该座位在所选时段已被预约");
                }

                reservationMapper.insert(reservation);
                return reservation;
            });
        }

        // 图书预约：检查重复预约
        if (reservation.getBookId() != null) {
            List<ReservationDTO> existing = reservationMapper.findByBookId(reservation.getBookId());
            boolean duplicate = existing.stream().anyMatch(r ->
                    r.getReaderId() != null && r.getReaderId().equals(reservation.getReaderId())
                    && (r.getStatus() == 0 || r.getStatus() == 1));
            if (duplicate) {
                throw new BusinessException("您已预约过该图书，请等待审批或取消已有预约");
            }
        }

        reservationMapper.insert(reservation);
        return reservation;
    }

    @Override
    public Reservation updateReservation(Reservation reservation) {
        reservationMapper.update(reservation);
        return reservation;
    }

    @Override
    public void deleteReservation(Integer id) {
        ReservationDTO reservation = reservationMapper.findById(id);
        if (reservation == null) {
            throw new BusinessException("预约记录不存在");
        }
        reservationMapper.delete(id);
    }

    @Override
    @Transactional
    public void updateReservationStatus(Integer id, Integer status) {
        ReservationDTO reservation = reservationMapper.findById(id);
        if (reservation == null) {
            throw new BusinessException("预约记录不存在");
        }

        Integer previousStatus = reservation.getStatus();
        reservationMapper.updateStatus(id, status);

        // 审批通过：处理座位/图书副作用
        if (status == 1 && previousStatus != 1) {
            // 审批通过前检查黑名单
            if (reservation.getReaderId() != null && blacklistService.isBlacklisted(reservation.getReaderId())) {
                throw new BusinessException("该读者已在黑名单中，无法审批通过");
            }
            if (reservation.getSeatId() != null) {
                seatService.updateSeatStatus(reservation.getSeatId(), 2);
            }
            if (reservation.getBookId() != null && reservation.getReaderId() != null) {
                com.library.entity.Reader reader = readerMapper.findById(reservation.getReaderId());
                if (reader != null) {
                    borrowingService.borrowBook(reader.getReaderId(), reservation.getBookId());
                }
            }
        }

        // 取消/拒绝已批准的预约：回滚座位、库存和借阅记录
        if ((status == 2 || status == 3) && previousStatus == 1) {
            // 违约累计
            if (reservation.getReaderId() != null) {
                blacklistService.incrementViolation(reservation.getReaderId());
            }
            if (reservation.getSeatId() != null) {
                seatService.updateSeatStatus(reservation.getSeatId(), 0);
            }
            if (reservation.getBookId() != null && reservation.getReaderId() != null) {
                com.library.entity.Borrowing activeBorrowing = borrowingMapper.findActiveByReaderAndBook(
                        reservation.getReaderId(), reservation.getBookId());
                if (activeBorrowing != null) {
                    borrowingMapper.updateStatus(activeBorrowing.getId(), 3); // 3: 已取消
                    readerMapper.decrementBorrowCount(activeBorrowing.getReaderId());
                }
                bookMapper.incrementAvailableCount(reservation.getBookId());
            }
        }
    }

    /**
     * 定时任务：每5分钟释放过期的预约
     */
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void releaseExpiredReservations() {
        List<ReservationDTO> expired = reservationMapper.findExpired(LocalDateTime.now());
        for (ReservationDTO reservation : expired) {
            try {
                reservationMapper.updateStatus(reservation.getId(), 4); // 4: 已过期
                // 释放座位
                if (reservation.getSeatId() != null) {
                    seatService.updateSeatStatus(reservation.getSeatId(), 0);
                }
                // 释放图书库存
                if (reservation.getBookId() != null) {
                    bookMapper.incrementAvailableCount(reservation.getBookId());
                }
                log.info("释放过期预约: id={}, readerId={}", reservation.getId(), reservation.getReaderId());
            } catch (Exception e) {
                log.error("释放过期预约失败: id={}", reservation.getId(), e);
            }
        }
    }
}

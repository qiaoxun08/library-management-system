package com.library.service;

import com.library.dto.ReservationDTO;
import com.library.entity.Borrowing;
import com.library.entity.Reader;
import com.library.entity.Reservation;
import com.library.exception.BusinessException;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowingMapper;
import com.library.mapper.ReaderMapper;
import com.library.mapper.ReservationMapper;
import com.library.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationService 单元测试")
class ReservationServiceTest {

    @Mock
    private ReservationMapper reservationMapper;

    @Mock
    private ReaderMapper readerMapper;

    @Mock
    private SeatService seatService;

    @Mock
    private BorrowingService borrowingService;

    @Mock
    private BookMapper bookMapper;

    @Mock
    private BorrowingMapper borrowingMapper;

    @Mock
    private BlacklistService blacklistService;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private Reader testReader;

    @BeforeEach
    void setUp() {
        testReader = new Reader();
        testReader.setId(1);
        testReader.setReaderId("2024001");
    }

    // ==================== 查询测试 ====================

    @Test
    @DisplayName("查询所有预约")
    void getAllReservations() {
        when(reservationMapper.findAll()).thenReturn(Collections.emptyList());

        List<ReservationDTO> result = reservationService.getAllReservations();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("查询预约 - 记录不存在")
    void getReservationById_NotFound() {
        when(reservationMapper.findById(999)).thenReturn(null);

        ReservationDTO result = reservationService.getReservationById(999);

        assertNull(result);
    }

    @Test
    @DisplayName("查询预约 - 记录存在")
    void getReservationById_Found() {
        ReservationDTO dto = new ReservationDTO();
        dto.setId(1);
        when(reservationMapper.findById(1)).thenReturn(dto);

        ReservationDTO result = reservationService.getReservationById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    @DisplayName("按读者ID查询预约")
    void getReservationsByReaderId() {
        when(reservationMapper.findByReaderId(1)).thenReturn(Collections.emptyList());

        List<ReservationDTO> result = reservationService.getReservationsByReaderId(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("按状态查询预约")
    void getReservationsByStatus() {
        when(reservationMapper.findByStatus(0)).thenReturn(Collections.emptyList());

        List<ReservationDTO> result = reservationService.getReservationsByStatus(0);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== 添加预约测试 ====================

    @Test
    @DisplayName("添加预约 - 正常添加（图书预约）")
    void addReservation_Success_Book() {
        Reservation reservation = new Reservation();
        reservation.setReaderId(1);
        reservation.setBookId(1);

        when(readerMapper.findById(1)).thenReturn(testReader);
        when(blacklistService.isBlacklisted(1)).thenReturn(false);
        when(reservationMapper.findByBookId(1)).thenReturn(Collections.emptyList());
        when(reservationMapper.insert(any(Reservation.class))).thenReturn(1);

        Reservation result = reservationService.addReservation(reservation);

        assertNotNull(result);
        verify(reservationMapper).insert(reservation);
    }

    @Test
    @DisplayName("添加预约 - 黑名单读者不能预约")
    void addReservation_Blacklisted() {
        Reservation reservation = new Reservation();
        reservation.setReaderId(1);
        reservation.setBookId(1);

        when(readerMapper.findById(1)).thenReturn(testReader);
        when(blacklistService.isBlacklisted(1)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> reservationService.addReservation(reservation));
        assertTrue(ex.getMessage().contains("黑名单"));
    }

    @Test
    @DisplayName("添加预约 - 重复预约图书")
    void addReservation_DuplicateBook() {
        Reservation reservation = new Reservation();
        reservation.setReaderId(1);
        reservation.setBookId(1);

        ReservationDTO existing = new ReservationDTO();
        existing.setReaderId(1);
        existing.setStatus(0); // 待审批

        when(readerMapper.findById(1)).thenReturn(testReader);
        when(blacklistService.isBlacklisted(1)).thenReturn(false);
        when(reservationMapper.findByBookId(1)).thenReturn(List.of(existing));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> reservationService.addReservation(reservation));
        assertTrue(ex.getMessage().contains("已预约"));
    }

    @Test
    @DisplayName("添加预约 - 重复预约座位")
    void addReservation_DuplicateSeat() {
        Reservation reservation = new Reservation();
        reservation.setReaderId(1);
        reservation.setSeatId(1);

        ReservationDTO existing = new ReservationDTO();
        existing.setReaderId(1);
        existing.setStatus(1); // 已批准

        when(readerMapper.findById(1)).thenReturn(testReader);
        when(blacklistService.isBlacklisted(1)).thenReturn(false);
        when(reservationMapper.findBySeatId(1)).thenReturn(List.of(existing));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> reservationService.addReservation(reservation));
        assertTrue(ex.getMessage().contains("已预约"));
    }

    // ==================== 删除预约测试 ====================

    @Test
    @DisplayName("删除预约 - 正常删除")
    void deleteReservation_Success() {
        ReservationDTO dto = new ReservationDTO();
        dto.setId(1);
        when(reservationMapper.findById(1)).thenReturn(dto);

        reservationService.deleteReservation(1);

        verify(reservationMapper).delete(1);
    }

    @Test
    @DisplayName("删除预约 - 记录不存在")
    void deleteReservation_NotFound() {
        when(reservationMapper.findById(999)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> reservationService.deleteReservation(999));
        assertEquals("预约记录不存在", ex.getMessage());
    }

    // ==================== 更新预约状态测试 ====================

    @Test
    @DisplayName("更新预约状态 - 审批通过（座位预约）")
    void updateReservationStatus_Approve_Seat() {
        ReservationDTO dto = new ReservationDTO();
        dto.setId(1);
        dto.setReaderId(1);
        dto.setSeatId(1);
        dto.setStatus(0); // 待审批

        when(reservationMapper.findById(1)).thenReturn(dto);
        when(blacklistService.isBlacklisted(1)).thenReturn(false);

        reservationService.updateReservationStatus(1, 1); // 审批通过

        verify(reservationMapper).updateStatus(1, 1);
        verify(seatService).updateSeatStatus(1, 2); // 座位状态改为已预约
    }

    @Test
    @DisplayName("更新预约状态 - 审批通过（图书预约，触发借阅）")
    void updateReservationStatus_Approve_Book() {
        ReservationDTO dto = new ReservationDTO();
        dto.setId(1);
        dto.setReaderId(1);
        dto.setBookId(1);
        dto.setStatus(0); // 待审批

        when(reservationMapper.findById(1)).thenReturn(dto);
        when(blacklistService.isBlacklisted(1)).thenReturn(false);
        when(readerMapper.findById(1)).thenReturn(testReader);
        when(borrowingService.borrowBook("2024001", 1)).thenReturn(new Borrowing());

        reservationService.updateReservationStatus(1, 1);

        verify(borrowingService).borrowBook("2024001", 1);
    }

    @Test
    @DisplayName("更新预约状态 - 审批通过但读者已在黑名单")
    void updateReservationStatus_Approve_Blacklisted() {
        ReservationDTO dto = new ReservationDTO();
        dto.setId(1);
        dto.setReaderId(1);
        dto.setBookId(1);
        dto.setStatus(0);

        when(reservationMapper.findById(1)).thenReturn(dto);
        when(blacklistService.isBlacklisted(1)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> reservationService.updateReservationStatus(1, 1));
        assertTrue(ex.getMessage().contains("黑名单"));
    }

    @Test
    @DisplayName("更新预约状态 - 取消已批准预约（座位）")
    void updateReservationStatus_Cancel_Approved_Seat() {
        ReservationDTO dto = new ReservationDTO();
        dto.setId(1);
        dto.setReaderId(1);
        dto.setSeatId(1);
        dto.setStatus(1); // 已批准

        when(reservationMapper.findById(1)).thenReturn(dto);

        reservationService.updateReservationStatus(1, 2); // 取消

        verify(reservationMapper).updateStatus(1, 2);
        verify(seatService).updateSeatStatus(1, 0); // 释放座位
        verify(blacklistService).incrementViolation(1); // 违约累计
    }

    @Test
    @DisplayName("更新预约状态 - 取消已批准预约（图书，回滚库存）")
    void updateReservationStatus_Cancel_Approved_Book() {
        ReservationDTO dto = new ReservationDTO();
        dto.setId(1);
        dto.setReaderId(1);
        dto.setBookId(1);
        dto.setStatus(1); // 已批准

        Borrowing activeBorrowing = new Borrowing();
        activeBorrowing.setId(10);
        activeBorrowing.setReaderId(1);

        when(reservationMapper.findById(1)).thenReturn(dto);
        when(borrowingMapper.findActiveByReaderAndBook(1, 1)).thenReturn(activeBorrowing);

        reservationService.updateReservationStatus(1, 3); // 拒绝

        verify(borrowingMapper).updateStatus(10, 3); // 借阅记录取消
        verify(readerMapper).decrementBorrowCount(1);
        verify(bookMapper).incrementAvailableCount(1);
        verify(blacklistService).incrementViolation(1);
    }

    @Test
    @DisplayName("更新预约状态 - 预约记录不存在")
    void updateReservationStatus_NotFound() {
        when(reservationMapper.findById(999)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> reservationService.updateReservationStatus(999, 1));
        assertEquals("预约记录不存在", ex.getMessage());
    }
}

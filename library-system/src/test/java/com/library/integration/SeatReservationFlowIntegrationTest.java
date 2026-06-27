package com.library.integration;

import com.library.entity.Reader;
import com.library.entity.Reservation;
import com.library.entity.Seat;
import com.library.exception.BusinessException;
import com.library.mapper.ReaderMapper;
import com.library.mapper.SeatMapper;
import com.library.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 座位预约流程集成测试
 * 覆盖：创建预约 → 审批通过 → 取消预约
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("座位预约流程集成测试")
class SeatReservationFlowIntegrationTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReaderMapper readerMapper;

    @Autowired
    private SeatMapper seatMapper;

    private Reader testReader;
    private Seat testSeat;

    @BeforeEach
    void setUp() {
        // 创建测试读者
        testReader = new Reader();
        testReader.setReaderId("SEAT001");
        testReader.setPassword("password");
        testReader.setRealName("座位测试读者");
        testReader.setDepartment("计算机学院");
        testReader.setMaxBorrowCount(5);
        testReader.setCurrentBorrowCount(0);
        testReader.setFineAmount(java.math.BigDecimal.ZERO);
        testReader.setStatus(1);
        readerMapper.insert(testReader);
        // 重新查询获取自动生成的 ID
        testReader = readerMapper.findByReaderId("SEAT001");

        // 创建测试座位
        testSeat = new Seat();
        testSeat.setSeatNumber("TEST-A001");
        testSeat.setArea("测试阅览室");
        testSeat.setStatus(0); // 空闲
        seatMapper.insert(testSeat);
        // 重新查询获取自动生成的 ID
        testSeat = seatMapper.findBySeatNumber("TEST-A001");
    }

    @Test
    @DisplayName("完整座位预约流程：创建 → 审批通过")
    void fullSeatReservationFlow() {
        // 1. 创建座位预约
        Reservation reservation = new Reservation();
        reservation.setReaderId(testReader.getId());
        reservation.setSeatId(testSeat.getId());
        reservation.setStatus(0); // 待审批

        Reservation created = reservationService.addReservation(reservation);

        assertNotNull(created);
        assertEquals(0, created.getStatus()); // 待审批

        // 2. 审批通过
        reservationService.updateReservationStatus(created.getId(), 1);

        // 验证预约状态更新
        var updated = reservationService.getReservationById(created.getId());
        assertEquals(1, updated.getStatus()); // 已批准
    }

    @Test
    @DisplayName("座位预约后取消")
    void seatReservationCancel() {
        // 1. 创建预约
        Reservation reservation = new Reservation();
        reservation.setReaderId(testReader.getId());
        reservation.setSeatId(testSeat.getId());
        reservation.setStatus(0); // 待审批

        Reservation created = reservationService.addReservation(reservation);

        // 2. 审批通过
        reservationService.updateReservationStatus(created.getId(), 1);

        // 3. 取消预约
        reservationService.updateReservationStatus(created.getId(), 2);

        var updated = reservationService.getReservationById(created.getId());
        assertEquals(2, updated.getStatus()); // 已取消
    }

    @Test
    @DisplayName("座位预约失败：重复预约同一座位")
    void duplicateSeatReservation() {
        // 1. 创建第一个预约
        Reservation reservation1 = new Reservation();
        reservation1.setReaderId(testReader.getId());
        reservation1.setSeatId(testSeat.getId());
        reservation1.setStatus(0); // 待审批
        reservationService.addReservation(reservation1);

        // 2. 尝试创建第二个预约同一座位
        Reservation reservation2 = new Reservation();
        reservation2.setReaderId(testReader.getId());
        reservation2.setSeatId(testSeat.getId());
        reservation2.setStatus(0);

        assertThrows(BusinessException.class,
                () -> reservationService.addReservation(reservation2));
    }

    @Test
    @DisplayName("查询读者预约记录")
    void getReaderReservations() {
        // 创建预约
        Reservation reservation = new Reservation();
        reservation.setReaderId(testReader.getId());
        reservation.setSeatId(testSeat.getId());
        reservation.setStatus(0);
        reservationService.addReservation(reservation);

        // 查询
        var reservations = reservationService.getReservationsByReaderId(testReader.getId());

        assertNotNull(reservations);
        assertFalse(reservations.isEmpty());
        assertEquals(1, reservations.size());
    }

    @Test
    @DisplayName("删除预约记录")
    void deleteReservation() {
        Reservation reservation = new Reservation();
        reservation.setReaderId(testReader.getId());
        reservation.setSeatId(testSeat.getId());

        Reservation created = reservationService.addReservation(reservation);

        reservationService.deleteReservation(created.getId());

        var result = reservationService.getReservationById(created.getId());
        assertNull(result);
    }

    @Test
    @DisplayName("预约不存在的记录时删除失败")
    void deleteNonExistentReservation() {
        assertThrows(BusinessException.class,
                () -> reservationService.deleteReservation(99999));
    }
}

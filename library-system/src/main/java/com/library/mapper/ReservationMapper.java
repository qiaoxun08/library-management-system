package com.library.mapper;

import com.library.dto.ReservationDTO;
import com.library.dto.ReservationStatusCount;
import com.library.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReservationMapper {
    ReservationDTO findById(@Param("id") Integer id);
    List<ReservationDTO> findAll();
    List<ReservationDTO> findByReaderId(@Param("readerId") Integer readerId);
    List<ReservationDTO> findByReaderIdString(@Param("readerId") String readerId);
    List<ReservationDTO> findByBookId(@Param("bookId") Integer bookId);
    List<ReservationDTO> findByStatus(@Param("status") Integer status);
    int insert(Reservation reservation);
    int update(Reservation reservation);
    int delete(@Param("id") Integer id);
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);
    List<ReservationDTO> findBySeatId(@Param("seatId") Integer seatId);
    List<ReservationDTO> findExpired(@Param("now") java.time.LocalDateTime now);
    int countTodayReservations();
    List<ReservationStatusCount> countByStatus();
    int countCancelledByReaderSince(@Param("readerId") Integer readerId, @Param("since") java.time.LocalDateTime since);
    int countByReaderIdAndStatus(@Param("readerId") Integer readerId, @Param("status") Integer status);

    /** 查询指定日期+时段已被预约的座位 ID 列表（status=0 待审批 或 1 已批准） */
    List<Integer> findReservedSeatIdsByDateAndSlot(@Param("date") String date, @Param("timeSlot") String timeSlot);
}

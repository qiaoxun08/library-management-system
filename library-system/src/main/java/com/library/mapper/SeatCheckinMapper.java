package com.library.mapper;

import com.library.entity.SeatCheckin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 座位签到Mapper
 */
@Mapper
public interface SeatCheckinMapper {
    List<SeatCheckin> findAll();
    SeatCheckin findById(@Param("id") Integer id);
    List<SeatCheckin> findBySeatId(@Param("seatId") Integer seatId);
    List<SeatCheckin> findByReaderId(@Param("readerId") Integer readerId);
    SeatCheckin findActiveByReaderId(@Param("readerId") Integer readerId);
    SeatCheckin findActiveBySeatId(@Param("seatId") Integer seatId);
    int insert(SeatCheckin seatCheckin);
    int update(SeatCheckin seatCheckin);
    int checkout(@Param("id") Integer id);
    int countByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);
    int countBySeatAndDateRange(@Param("seatId") Integer seatId, @Param("startDate") String startDate, @Param("endDate") String endDate);
    List<java.util.Map<String, Object>> findCheckinStatsByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);
    List<java.util.Map<String, Object>> findReaderPreferredTimeSlots(@Param("readerId") Integer readerId);
    List<java.util.Map<String, Object>> findReaderPreferredAreas(@Param("readerId") Integer readerId);
    List<java.util.Map<String, Object>> findHeatmapData();
    int countActiveSeatsAtTime(@Param("dayOfWeek") int dayOfWeek, @Param("hour") int hour);
    int countTotalCheckedIn();
    int countCheckinsBySeatAndDateRange(@Param("seatId") Integer seatId, @Param("startDate") String startDate, @Param("endDate") String endDate);
    List<java.util.Map<String, Object>> findPreferredSlotsByReader(@Param("readerId") Integer readerId);
}

package com.library.mapper;

import com.library.entity.Seat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SeatMapper {
    Seat findById(@Param("id") Integer id);
    Seat findBySeatNumber(@Param("seatNumber") String seatNumber);
    List<Seat> findAll();
    List<Seat> findByArea(@Param("area") String area);
    List<Seat> findByStatus(@Param("status") Integer status);
    int insert(Seat seat);
    int update(Seat seat);
    int delete(@Param("id") Integer id);
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);
    int countTotalSeats();
    int countAvailableSeats();
    int countByAreaAndStatus(@Param("area") String area, @Param("status") Integer status);
}

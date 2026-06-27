package com.library.mapper;

import com.library.entity.Scheduling;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SchedulingMapper {
    Scheduling findById(@Param("id") Integer id);
    List<Scheduling> findAll();
    List<Scheduling> findBySeatId(@Param("seatId") Integer seatId);
    List<Scheduling> findByReaderId(@Param("readerId") Integer readerId);
    List<Scheduling> findByStatus(@Param("status") Integer status);
    int insert(Scheduling scheduling);
    int update(Scheduling scheduling);
    int delete(@Param("id") Integer id);
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);
}

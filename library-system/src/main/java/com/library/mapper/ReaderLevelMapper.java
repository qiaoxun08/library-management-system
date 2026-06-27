package com.library.mapper;

import com.library.entity.ReaderLevel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 读者等级积分Mapper
 */
@Mapper
public interface ReaderLevelMapper {
    List<ReaderLevel> findAll();
    ReaderLevel findByReaderId(@Param("readerId") Integer readerId);
    int insert(ReaderLevel readerLevel);
    int update(ReaderLevel readerLevel);
    int addPoints(@Param("readerId") Integer readerId, @Param("points") Integer points);
    int subtractPoints(@Param("readerId") Integer readerId, @Param("points") Integer points);
    int incrementBorrowCount(@Param("readerId") Integer readerId);
    int incrementReturnOnTime(@Param("readerId") Integer readerId);
}

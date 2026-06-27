package com.library.mapper;

import com.library.entity.Blacklist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 黑名单Mapper
 */
@Mapper
public interface BlacklistMapper {
    List<Blacklist> findAll();
    Blacklist findById(@Param("id") Integer id);
    Blacklist findByReaderId(@Param("readerId") Integer readerId);
    List<Blacklist> findActiveBlacklist();
    int insert(Blacklist blacklist);
    int update(Blacklist blacklist);
    int delete(@Param("id") Integer id);
    int incrementViolationCount(@Param("readerId") Integer readerId);
}

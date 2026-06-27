package com.library.mapper;

import com.library.entity.ReaderFollow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 读者关注关系Mapper
 */
@Mapper
public interface ReaderFollowMapper {
    int insert(ReaderFollow follow);
    int deleteByPair(@Param("followerId") Integer followerId, @Param("followeeId") Integer followeeId);
    int existsByPair(@Param("followerId") Integer followerId, @Param("followeeId") Integer followeeId);
    List<Map<String, Object>> findFollowers(@Param("followeeId") Integer followeeId);
    List<Map<String, Object>> findFollowees(@Param("followerId") Integer followerId);
    int countFollowers(@Param("followeeId") Integer followeeId);
    int countFollowees(@Param("followerId") Integer followerId);
}

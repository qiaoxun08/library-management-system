package com.library.mapper;

import com.library.entity.ReviewLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 评论点赞Mapper
 */
@Mapper
public interface ReviewLikeMapper {
    int insert(ReviewLike like);
    int deleteByReviewAndReader(@Param("reviewId") Integer reviewId, @Param("readerId") Integer readerId);
    int existsByReviewAndReader(@Param("reviewId") Integer reviewId, @Param("readerId") Integer readerId);
    int countByReviewId(@Param("reviewId") Integer reviewId);
}

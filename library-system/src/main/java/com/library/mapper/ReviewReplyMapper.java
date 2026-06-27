package com.library.mapper;

import com.library.entity.ReviewReply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReviewReplyMapper {

    List<ReviewReply> findByReviewId(@Param("reviewId") Integer reviewId);

    ReviewReply findById(@Param("id") Integer id);

    int insert(ReviewReply reply);

    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    int countByReviewId(@Param("reviewId") Integer reviewId);

    int deleteByReviewId(@Param("reviewId") Integer reviewId);
}

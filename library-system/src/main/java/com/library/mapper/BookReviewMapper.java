package com.library.mapper;

import com.library.entity.BookReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 图书评论Mapper
 */
@Mapper
public interface BookReviewMapper {
    int insert(BookReview review);
    int deleteById(@Param("id") Integer id);
    List<BookReview> findByBookId(@Param("bookId") Integer bookId);
    List<BookReview> findByReaderId(@Param("readerId") Integer readerId);
    int countByBookId(@Param("bookId") Integer bookId);
    BookReview findById(@Param("id") Integer id);

    /** 联表查询图书评论（含 readerName + likeCount），避免 N+1 */
    List<Map<String, Object>> findBookReviewsWithDetail(@Param("bookId") Integer bookId);

    /** 联表查询读者评论（含 bookTitle + likeCount），避免 N+1 */
    List<Map<String, Object>> findReaderReviewsWithDetail(@Param("readerId") Integer readerId);

    /** 批量查询图书平均评分（用于推荐算法 peer_rating 维度） */
    List<Map<String, Object>> findAvgRatingByBookIds(@Param("bookIds") List<Integer> bookIds);
}

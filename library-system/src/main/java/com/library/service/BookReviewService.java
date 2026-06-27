package com.library.service;

import java.util.Map;

/**
 * 图书评论服务接口
 */
public interface BookReviewService {
    /**
     * 添加评论
     */
    Map<String, Object> addReview(Integer bookId, Integer readerId, String content, Integer rating);

    /**
     * 删除评论（读者只能删自己的，管理员/馆员可删任意）
     */
    void deleteReview(Integer reviewId, Integer readerId, boolean isAdminOrLibrarian);

    /**
     * 分页获取图书评论
     */
    Map<String, Object> getBookReviews(Integer bookId, int page, int size);

    /**
     * 获取读者的所有评论
     */
    Map<String, Object> getReaderReviews(Integer readerId);

    /**
     * 点赞评论
     */
    void likeReview(Integer reviewId, Integer readerId);

    /**
     * 取消点赞
     */
    void unlikeReview(Integer reviewId, Integer readerId);

    /**
     * 检查是否已点赞
     */
    boolean hasLiked(Integer reviewId, Integer readerId);
}

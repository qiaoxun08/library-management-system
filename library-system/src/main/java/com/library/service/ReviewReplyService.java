package com.library.service;

import com.library.entity.ReviewReply;

import java.util.Map;

/**
 * 书评回复服务接口
 */
public interface ReviewReplyService {

    /**
     * 发表回复
     */
    ReviewReply addReply(Integer reviewId, Integer readerId, String content, Integer replyToReaderId);

    /**
     * 删除回复（ownership 校验）
     */
    void deleteReply(Integer replyId, Integer readerId, boolean isAdminOrLibrarian);

    /**
     * 获取书评的回复列表（分页）
     */
    Map<String, Object> getReplies(Integer reviewId, int page, int size);

    /**
     * 级联软删除书评下的所有回复
     */
    void deleteByReviewId(Integer reviewId);
}

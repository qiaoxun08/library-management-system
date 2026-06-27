package com.library.service.impl;

import com.library.entity.ReviewReply;
import com.library.exception.BusinessException;
import com.library.mapper.ReviewReplyMapper;
import com.library.service.ReviewReplyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 书评回复服务实现
 */
@Service
public class ReviewReplyServiceImpl implements ReviewReplyService {

    private static final Logger log = LoggerFactory.getLogger(ReviewReplyServiceImpl.class);

    @Autowired
    private ReviewReplyMapper reviewReplyMapper;

    @Override
    @Transactional
    public ReviewReply addReply(Integer reviewId, Integer readerId, String content, Integer replyToReaderId) {
        ReviewReply reply = new ReviewReply();
        reply.setReviewId(reviewId);
        reply.setReaderId(readerId);
        reply.setContent(content);
        reply.setReplyToReaderId(replyToReaderId);
        reply.setStatus(1);

        reviewReplyMapper.insert(reply);
        log.info("添加书评回复: reviewId={}, readerId={}, replyId={}", reviewId, readerId, reply.getId());

        // 重新查询获取完整信息（含回复者姓名）
        return reviewReplyMapper.findById(reply.getId());
    }

    @Override
    @Transactional
    public void deleteReply(Integer replyId, Integer readerId, boolean isAdminOrLibrarian) {
        ReviewReply reply = reviewReplyMapper.findById(replyId);
        if (reply == null) {
            throw new BusinessException("回复记录不存在");
        }

        // ownership 校验：读者只能删自己的
        if (!isAdminOrLibrarian && !reply.getReaderId().equals(readerId)) {
            throw new BusinessException(403, "无权删除他人的回复");
        }

        reviewReplyMapper.updateStatus(replyId, 0);
        log.info("删除书评回复: replyId={}, readerId={}", replyId, readerId);
    }

    @Override
    public Map<String, Object> getReplies(Integer reviewId, int page, int size) {
        List<ReviewReply> allReplies = reviewReplyMapper.findByReviewId(reviewId);
        int total = allReplies.size();

        // 内存分页
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, total);
        List<ReviewReply> items = fromIndex < total ? allReplies.subList(fromIndex, toIndex) : List.of();

        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    @Override
    @Transactional
    public void deleteByReviewId(Integer reviewId) {
        reviewReplyMapper.deleteByReviewId(reviewId);
        log.info("级联软删除书评回复: reviewId={}", reviewId);
    }
}

package com.library.service.impl;

import com.library.entity.BookReview;
import com.library.entity.Notification;
import com.library.entity.Reader;
import com.library.entity.ReviewLike;
import com.library.exception.BusinessException;
import com.library.mapper.BookReviewMapper;
import com.library.mapper.NotificationMapper;
import com.library.mapper.ReaderMapper;
import com.library.mapper.ReviewLikeMapper;
import com.library.service.BookReviewService;
import com.library.service.ReviewReplyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 图书评论服务实现
 */
@Service
public class BookReviewServiceImpl implements BookReviewService {

    private static final Logger log = LoggerFactory.getLogger(BookReviewServiceImpl.class);

    @Autowired
    private BookReviewMapper bookReviewMapper;

    @Autowired
    private ReviewLikeMapper reviewLikeMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private ReaderMapper readerMapper;

    @Autowired
    private ReviewReplyService reviewReplyService;

    @Override
    @Transactional
    public Map<String, Object> addReview(Integer bookId, Integer readerId, String content, Integer rating) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("评论内容不能为空");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw new BusinessException("评分范围为1-5");
        }

        Reader reader = readerMapper.findById(readerId);
        if (reader == null) {
            throw new BusinessException("读者不存在");
        }

        BookReview review = new BookReview();
        review.setBookId(bookId);
        review.setReaderId(readerId);
        review.setContent(content.trim());
        review.setRating(rating);
        bookReviewMapper.insert(review);

        // 构建返回结果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", review.getId());
        result.put("bookId", bookId);
        result.put("readerId", readerId);
        result.put("readerName", reader.getRealName());
        result.put("content", content.trim());
        result.put("rating", rating);
        result.put("likeCount", 0);
        result.put("createTime", review.getCreateTime());

        log.info("添加图书评论: bookId={}, readerId={}, rating={}", bookId, readerId, rating);
        return result;
    }

    @Override
    @Transactional
    public void deleteReview(Integer reviewId, Integer readerId, boolean isAdminOrLibrarian) {
        BookReview review = bookReviewMapper.findById(reviewId);
        if (review == null) {
            throw new BusinessException("评论不存在");
        }
        if (!isAdminOrLibrarian) {
            if (readerId == null || !review.getReaderId().equals(readerId)) {
                throw new BusinessException("只能删除自己的评论");
            }
        }

        // 管理员/馆员删除时，通知评论作者
        if (isAdminOrLibrarian && review.getReaderId() != null) {
            Notification notification = new Notification();
            notification.setReaderId(review.getReaderId());
            notification.setType("system");
            notification.setTitle("书评被删除");
            notification.setContent("您的一条书评因违反社区规则已被管理员删除。如有疑问请联系图书馆管理员。");
            notification.setIsRead(0);
            notificationMapper.insert(notification);
        }

        bookReviewMapper.deleteById(reviewId);
        // 级联软删除该书评下的所有回复
        reviewReplyService.deleteByReviewId(reviewId);
        log.info("删除评论: reviewId={}, readerId={}, admin={}", reviewId, readerId, isAdminOrLibrarian);
    }

    @Override
    public Map<String, Object> getBookReviews(Integer bookId, int page, int size) {
        // 使用联表查询，避免 N+1
        List<Map<String, Object>> allReviews = bookReviewMapper.findBookReviewsWithDetail(bookId);
        int total = allReviews.size();
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, total);

        List<Map<String, Object>> items = fromIndex < total
                ? new ArrayList<>(allReviews.subList(fromIndex, toIndex))
                : new ArrayList<>();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", items);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    @Override
    public Map<String, Object> getReaderReviews(Integer readerId) {
        // 使用联表查询，避免 N+1
        List<Map<String, Object>> items = bookReviewMapper.findReaderReviewsWithDetail(readerId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", items);
        result.put("total", items.size());
        return result;
    }

    @Override
    @Transactional
    public void likeReview(Integer reviewId, Integer readerId) {
        BookReview review = bookReviewMapper.findById(reviewId);
        if (review == null) {
            throw new BusinessException("评论不存在");
        }

        // 检查是否已点赞
        int exists = reviewLikeMapper.existsByReviewAndReader(reviewId, readerId);
        if (exists > 0) {
            throw new BusinessException("已经点赞过了");
        }

        // 插入点赞记录
        ReviewLike like = new ReviewLike();
        like.setReviewId(reviewId);
        like.setReaderId(readerId);
        reviewLikeMapper.insert(like);

        // 发送通知给评论作者（非自己点赞时）
        if (!review.getReaderId().equals(readerId)) {
            Reader liker = readerMapper.findById(readerId);
            String readerName = liker != null ? liker.getRealName() : "某读者";
            Notification notification = new Notification();
            notification.setReaderId(review.getReaderId());
            notification.setType("social");
            notification.setTitle("收到点赞");
            notification.setContent(readerName + " 赞了您的评论");
            notification.setIsRead(0);
            notificationMapper.insert(notification);
        }

        log.info("点赞评论: reviewId={}, readerId={}", reviewId, readerId);
    }

    @Override
    @Transactional
    public void unlikeReview(Integer reviewId, Integer readerId) {
        int exists = reviewLikeMapper.existsByReviewAndReader(reviewId, readerId);
        if (exists == 0) {
            throw new BusinessException("尚未点赞");
        }

        reviewLikeMapper.deleteByReviewAndReader(reviewId, readerId);

        log.info("取消点赞: reviewId={}, readerId={}", reviewId, readerId);
    }

    @Override
    public boolean hasLiked(Integer reviewId, Integer readerId) {
        return reviewLikeMapper.existsByReviewAndReader(reviewId, readerId) > 0;
    }
}

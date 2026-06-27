package com.library.service.impl;

import com.library.entity.Notification;
import com.library.entity.Reader;
import com.library.entity.ReaderFollow;
import com.library.exception.BusinessException;
import com.library.mapper.NotificationMapper;
import com.library.mapper.ReaderFollowMapper;
import com.library.mapper.ReaderMapper;
import com.library.service.ReaderFollowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 读者关注服务实现
 */
@Service
public class ReaderFollowServiceImpl implements ReaderFollowService {

    private static final Logger log = LoggerFactory.getLogger(ReaderFollowServiceImpl.class);

    @Autowired
    private ReaderFollowMapper readerFollowMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private ReaderMapper readerMapper;

    @Override
    @Transactional
    public void follow(Integer followerId, Integer followeeId) {
        if (followerId.equals(followeeId)) {
            throw new BusinessException("不能关注自己");
        }

        // 检查被关注者是否存在
        Reader followee = readerMapper.findById(followeeId);
        if (followee == null) {
            throw new BusinessException("目标读者不存在");
        }

        // 检查是否已关注
        int exists = readerFollowMapper.existsByPair(followerId, followeeId);
        if (exists > 0) {
            throw new BusinessException("已经关注了该读者");
        }

        // 创建关注关系
        ReaderFollow follow = new ReaderFollow();
        follow.setFollowerId(followerId);
        follow.setFolloweeId(followeeId);
        readerFollowMapper.insert(follow);

        // 发送通知给被关注者
        Reader follower = readerMapper.findById(followerId);
        String followerName = follower != null ? follower.getRealName() : "某读者";
        Notification notification = new Notification();
        notification.setReaderId(followeeId);
        notification.setType("social");
        notification.setTitle("新关注者");
        notification.setContent(followerName + " 关注了你");
        notification.setIsRead(0);
        notificationMapper.insert(notification);

        log.info("读者关注: followerId={}, followeeId={}", followerId, followeeId);
    }

    @Override
    @Transactional
    public void unfollow(Integer followerId, Integer followeeId) {
        int exists = readerFollowMapper.existsByPair(followerId, followeeId);
        if (exists == 0) {
            throw new BusinessException("未关注该读者");
        }
        readerFollowMapper.deleteByPair(followerId, followeeId);
        log.info("取消关注: followerId={}, followeeId={}", followerId, followeeId);
    }

    @Override
    public boolean isFollowing(Integer followerId, Integer followeeId) {
        return readerFollowMapper.existsByPair(followerId, followeeId) > 0;
    }

    @Override
    public Map<String, Object> getFollowers(Integer readerId) {
        List<Map<String, Object>> followers = readerFollowMapper.findFollowers(readerId);
        int count = readerFollowMapper.countFollowers(readerId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", followers);
        result.put("total", count);
        return result;
    }

    @Override
    public Map<String, Object> getFollowees(Integer readerId) {
        List<Map<String, Object>> followees = readerFollowMapper.findFollowees(readerId);
        int count = readerFollowMapper.countFollowees(readerId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", followees);
        result.put("total", count);
        return result;
    }
}

package com.library.service;

import com.library.entity.Notification;

import java.util.List;

/**
 * 通知消息服务接口
 */
public interface NotificationService {
    List<Notification> getNotificationsByReaderId(Integer readerId);
    Notification getNotificationById(Integer id);
    int countUnread(Integer readerId);
    void sendNotification(Notification notification);
    void markAsRead(Integer id);
    void markAllAsRead(Integer readerId);

    /**
     * 发送社交类通知（评论、点赞、关注等）
     */
    void sendSocialNotification(Integer readerId, String type, String title, String content);
}

package com.library.mapper;

import com.library.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知消息Mapper
 */
@Mapper
public interface NotificationMapper {
    List<Notification> findByReaderId(@Param("readerId") Integer readerId);
    Notification findById(@Param("id") Integer id);
    int insert(Notification notification);
    int markAsRead(@Param("id") Integer id);
    int markAllAsRead(@Param("readerId") Integer readerId);
    int delete(@Param("id") Integer id);
    int countUnread(@Param("readerId") Integer readerId);
    int insertNotification(Notification notification);
}

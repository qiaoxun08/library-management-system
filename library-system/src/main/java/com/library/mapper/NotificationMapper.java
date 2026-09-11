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
    int markAsRead(@Param("id") Integer id, @Param("readerId") Integer readerId);
    int markAllAsRead(@Param("readerId") Integer readerId);
    int delete(@Param("id") Integer id);
    int countUnread(@Param("readerId") Integer readerId);

    /** 统计读者今天已发过的同标题关键字通知数（用于去重，替代全量加载） */
    int countTodayByReaderIdAndKeyword(@Param("readerId") Integer readerId, @Param("keyword") String keyword);
    int insertNotification(Notification notification);
}

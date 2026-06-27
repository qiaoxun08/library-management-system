package com.library.service.impl;

import com.library.entity.Borrowing;
import com.library.entity.Notification;
import com.library.exception.BusinessException;
import com.library.mapper.BorrowingMapper;
import com.library.mapper.NotificationMapper;
import com.library.service.NotificationService;
import com.library.service.SystemConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 通知消息服务实现
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private BorrowingMapper borrowingMapper;

    @Autowired
    private SystemConfigService systemConfigService;

    @Override
    public List<Notification> getNotificationsByReaderId(Integer readerId) {
        return notificationMapper.findByReaderId(readerId);
    }

    @Override
    public Notification getNotificationById(Integer id) {
        Notification notification = notificationMapper.findById(id);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        return notification;
    }

    @Override
    public int countUnread(Integer readerId) {
        return notificationMapper.countUnread(readerId);
    }

    @Override
    public void sendNotification(Notification notification) {
        notificationMapper.insert(notification);
    }

    @Override
    public void markAsRead(Integer id) {
        notificationMapper.markAsRead(id);
    }

    @Override
    public void markAllAsRead(Integer readerId) {
        notificationMapper.markAllAsRead(readerId);
    }

    @Override
    public void sendSocialNotification(Integer readerId, String type, String title, String content) {
        Notification notification = new Notification();
        notification.setReaderId(readerId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
        log.info("发送社交通知: readerId={}, type={}, title={}", readerId, type, title);
    }

    /**
     * 定时任务：每天9点检查即将到期和已逾期的借阅，发送催还通知（梯度提醒 + 去重）
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkOverdueNotifications() {
        log.info("开始检查即将到期和已逾期的借阅...");
        String remindDaysStr = systemConfigService.getConfigValue("library.notification.borrow-remind-days");
        int remindDays = remindDaysStr != null ? Integer.parseInt(remindDaysStr) : 3;

        List<Borrowing> borrowings = borrowingMapper.findDueSoonOrOverdue(remindDays);
        int sentCount = 0;

        for (Borrowing borrowing : borrowings) {
            long daysUntilDue = ChronoUnit.DAYS.between(LocalDateTime.now(), borrowing.getDueDate());

            // 梯度通知策略：根据逾期天数决定通知频率，避免重复打扰
            String notifyLevel = determineNotifyLevel(daysUntilDue);
            if (notifyLevel == null) {
                continue; // 当前天数不在通知窗口
            }

            // 去重：检查今天是否已发过同级别通知
            if (hasNotificationToday(borrowing.getReaderId(), notifyLevel)) {
                continue;
            }

            Notification notification = new Notification();
            notification.setReaderId(borrowing.getReaderId());
            notification.setType("system");

            switch (notifyLevel) {
                case "due_tomorrow":
                    notification.setTitle("温馨提醒：明天到期");
                    notification.setContent("您借阅的图书将于明天到期，请及时归还或续借。");
                    break;
                case "overdue_1_3":
                    notification.setTitle("逾期提醒");
                    notification.setContent("您借阅的图书已逾期" + Math.abs(daysUntilDue) + "天，请尽快归还。");
                    break;
                case "overdue_4_7":
                    notification.setTitle("紧急催还");
                    notification.setContent("您借阅的图书已逾期" + Math.abs(daysUntilDue) + "天，继续逾期将影响借阅权限，请立即归还。");
                    break;
                case "overdue_7_plus":
                    notification.setTitle("严重逾期警告");
                    notification.setContent("您借阅的图书已逾期" + Math.abs(daysUntilDue) + "天，已产生较高罚款，请立即归还并缴纳罚款。");
                    break;
            }

            notification.setIsRead(0);
            notificationMapper.insert(notification);
            sentCount++;
            log.info("发送催还通知: readerId={}, borrowingId={}, level={}", borrowing.getReaderId(), borrowing.getId(), notifyLevel);
        }
        log.info("催还通知检查完成，共处理{}条记录，发送{}条通知", borrowings.size(), sentCount);
    }

    /**
     * 根据距到期天数确定通知级别
     * @return 通知级别 key，null 表示不发通知
     */
    private String determineNotifyLevel(long daysUntilDue) {
        if (daysUntilDue == 1) return "due_tomorrow";          // 到期前1天
        if (daysUntilDue >= -3 && daysUntilDue <= -1) return "overdue_1_3"; // 逾期1-3天
        if (daysUntilDue >= -7 && daysUntilDue <= -4) return "overdue_4_7"; // 逾期4-7天
        if (daysUntilDue < -7) return "overdue_7_plus";        // 逾期7天以上
        return null; // 到期前2天以上不发通知
    }

    /**
     * 检查今天是否已发过同级别通知（去重）
     */
    private boolean hasNotificationToday(Integer readerId, String level) {
        // 简单策略：通过标题关键字判断
        String titleKeyword;
        switch (level) {
            case "due_tomorrow": titleKeyword = "明天到期"; break;
            case "overdue_1_3": titleKeyword = "逾期提醒"; break;
            case "overdue_4_7": titleKeyword = "紧急催还"; break;
            case "overdue_7_plus": titleKeyword = "严重逾期"; break;
            default: return false;
        }
        List<Notification> todayNotifs = notificationMapper.findByReaderId(readerId);
        return todayNotifs.stream().anyMatch(n ->
                n.getTitle() != null && n.getTitle().contains(titleKeyword)
                && n.getCreateTime() != null
                && n.getCreateTime().toLocalDate().equals(LocalDateTime.now().toLocalDate()));
    }
}

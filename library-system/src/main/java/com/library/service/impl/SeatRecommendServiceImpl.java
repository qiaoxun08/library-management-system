package com.library.service.impl;

import com.library.entity.Blacklist;
import com.library.entity.Notification;
import com.library.entity.Reader;
import com.library.entity.Seat;
import com.library.entity.StudyBuddy;
import com.library.mapper.*;
import com.library.service.SeatRecommendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 智能座位推荐服务实现
 */
@Service
public class SeatRecommendServiceImpl implements SeatRecommendService {

    private static final Logger log = LoggerFactory.getLogger(SeatRecommendServiceImpl.class);

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private SeatCheckinMapper seatCheckinMapper;

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private ReaderMapper readerMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private BlacklistMapper blacklistMapper;

    @Autowired
    private StudyBuddyMapper studyBuddyMapper;

    @Override
    public List<Map<String, Object>> recommendSeats(Integer readerId, String date, String timeSlot, int limit) {
        log.info("为读者{}推荐座位，日期：{}，时段：{}，数量：{}", readerId, date, timeSlot, limit);

        // 获取读者偏好的区域
        List<Map<String, Object>> preferredAreas = seatCheckinMapper.findReaderPreferredAreas(readerId);
        Set<String> preferredAreaNames = new HashSet<>();
        for (Map<String, Object> area : preferredAreas) {
            String areaName = (String) area.get("area");
            if (areaName != null) {
                preferredAreaNames.add(areaName);
            }
        }

        // 获取所有空闲座位
        List<Seat> availableSeats = seatMapper.findByStatus(0); // 0=空闲

        // 过滤：排除指定时段已被预约的座位
        if (timeSlot != null && !timeSlot.isEmpty() && date != null) {
            List<Integer> reservedSeatIds = reservationMapper.findReservedSeatIdsByDateAndSlot(date, timeSlot);
            if (reservedSeatIds != null && !reservedSeatIds.isEmpty()) {
                availableSeats.removeIf(seat -> reservedSeatIds.contains(seat.getId()));
            }
        }

        // 获取学习伙伴偏好区域
        StudyBuddy buddyProfile = studyBuddyMapper.findByReaderId(readerId);
        String buddyPreferredArea = buddyProfile != null ? buddyProfile.getPreferredArea() : null;

        // 获取读者历史使用时段（避免N+1查询）
        List<Map<String, Object>> readerPreferredTimeSlots = seatCheckinMapper.findReaderPreferredTimeSlots(readerId);
        boolean hasTimePreference = !readerPreferredTimeSlots.isEmpty();

        // 构建推荐列表
        List<Map<String, Object>> recommendations = new ArrayList<>();
        for (Seat seat : availableSeats) {
            if (recommendations.size() >= limit) {
                break;
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("seatId", seat.getId());
            item.put("seatNumber", seat.getSeatNumber());
            item.put("area", seat.getArea());

            // 计算推荐分数和理由
            int score = 50; // 基础分
            StringBuilder reason = new StringBuilder();

            // 偏好区域加分
            if (preferredAreaNames.contains(seat.getArea())) {
                score += 30;
                if (reason.length() > 0) reason.append("，");
                reason.append("位于您常去的").append(seat.getArea());
            }

            // 历史使用频率加分（通过签到记录）
            if (hasTimePreference) {
                score += 10;
                if (reason.length() > 0) reason.append("，");
                reason.append("符合您的使用习惯");
            }

            // 学习伙伴偏好区域加分
            if (buddyPreferredArea != null && buddyPreferredArea.equals(seat.getArea())) {
                score += 15;
                if (reason.length() > 0) reason.append("，");
                reason.append("与学习伙伴偏好区域一致");
            }

            if (reason.length() == 0) {
                reason.append("当前空闲");
            }

            item.put("score", score);
            item.put("reason", reason.toString());
            recommendations.add(item);
        }

        // 按分数降序排列
        recommendations.sort((a, b) -> Integer.compare((int) b.get("score"), (int) a.get("score")));

        log.info("座位推荐完成: readerId={}, 推荐数量={}", readerId, recommendations.size());
        return recommendations;
    }

    @Override
    @Transactional
    public void detectMaliciousCancels() {
        log.info("开始检测恶意取消预约行为...");

        // 获取所有读者
        List<Reader> readers = readerMapper.findAll();
        int detectedCount = 0;

        for (Reader reader : readers) {
            // 统计过去7天内取消的预约数量（status=3为已取消）
            LocalDateTime since = LocalDateTime.now().minusDays(7);
            int cancelCount = reservationMapper.countCancelledByReaderSince(reader.getId(), since);

            // 如果取消次数>=3，记录违规
            if (cancelCount >= 3) {
                log.warn("检测到恶意取消预约: readerId={}, cancelCount={}", reader.getId(), cancelCount);

                // 增加违规次数（BlacklistMapper已有此方法）
                Blacklist existing = blacklistMapper.findByReaderId(reader.getId());
                if (existing == null) {
                    // 创建新的黑名单记录
                    Blacklist blacklist = new Blacklist();
                    blacklist.setReaderId(reader.getId());
                    blacklist.setReason("7天内取消预约" + cancelCount + "次，涉嫌恶意取消");
                    blacklist.setViolationCount(1);
                    blacklist.setBlacklisted(0);
                    blacklist.setStartTime(LocalDateTime.now());
                    blacklistMapper.insert(blacklist);
                } else {
                    blacklistMapper.incrementViolationCount(reader.getId());
                }

                // 发送警告通知
                Notification notification = new Notification();
                notification.setReaderId(reader.getId());
                notification.setType("warning");
                notification.setTitle("预约行为警告");
                notification.setContent("系统检测到您近期频繁取消座位预约，请注意合理使用预约功能。");
                notification.setIsRead(0);
                notificationMapper.insert(notification);

                detectedCount++;
            }
        }

        log.info("恶意取消预约检测完成，检测到{}名读者", detectedCount);
    }
}

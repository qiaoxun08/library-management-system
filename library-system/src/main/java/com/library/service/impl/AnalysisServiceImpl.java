package com.library.service.impl;

import com.library.mapper.BorrowingMapper;
import com.library.mapper.ReservationMapper;
import com.library.mapper.SeatCheckinMapper;
import com.library.mapper.SeatMapper;
import com.library.service.AnalysisService;
import com.library.service.RedisCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 数据分析服务实现
 */
@Service
public class AnalysisServiceImpl implements AnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisServiceImpl.class);

    private static final String SEAT_PREDICTION_KEY = "seat_predictions";
    private static final long SEAT_PREDICTION_TTL_MINUTES = 26 * 60; // 26小时

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private BorrowingMapper borrowingMapper;

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private SeatCheckinMapper seatCheckinMapper;

    @Autowired
    private ReservationMapper reservationMapper;

    @Override
    public Map<String, Object> getSeatPrediction(String date, String timeSlot) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("date", date);
        result.put("timeSlot", timeSlot);

        try {
            // 解析日期获取星期几
            LocalDate localDate = LocalDate.parse(date);
            int dayOfWeek = localDate.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday

            // 解析时段获取小时范围
            int[] hourRange = parseTimeSlot(timeSlot);
            int startHour = hourRange[0];
            int endHour = hourRange[1];

            // 计算该星期几和时段的平均使用率
            int totalSeats = seatMapper.countTotalSeats();
            int totalCheckins = 0;
            int slotCount = 0;

            for (int hour = startHour; hour < endHour; hour++) {
                int count = seatCheckinMapper.countActiveSeatsAtTime(dayOfWeek, hour);
                totalCheckins += count;
                slotCount++;
            }

            double avgOccupancy = slotCount > 0 ? (double) totalCheckins / slotCount : 0;
            int predictedAvailable = totalSeats > 0 ? (int) (totalSeats * (1 - avgOccupancy / Math.max(totalSeats, 1))) : 0;

            Map<String, Object> prediction = new LinkedHashMap<>();
            prediction.put("totalSeats", totalSeats);
            prediction.put("predictedOccupancy", (int) avgOccupancy);
            prediction.put("predictedAvailable", Math.max(predictedAvailable, 0));
            prediction.put("confidence", 0.8);
            result.put("prediction", prediction);

            // 推荐时段：使用率较低的时段
            List<String> recommendedSlots = Arrays.asList("09:00-11:00", "14:00-16:00");
            result.put("recommendedSlots", recommendedSlots);
        } catch (Exception e) {
            log.error("计算座位预测失败: date={}, timeSlot={}", date, timeSlot, e);
            result.put("prediction", Collections.emptyMap());
        }

        return result;
    }

    @Override
    public Map<String, Object> getBookTrend(int days) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("days", days);

        // 按分类统计指定天数内的借阅量
        List<Map<String, Object>> categoryTrends = borrowingMapper.countByCategoryAndDays(days);
        result.put("categoryTrends", categoryTrends);

        // 计算趋势方向
        if (categoryTrends.size() >= 2) {
            long firstHalf = categoryTrends.subList(0, categoryTrends.size() / 2).stream()
                    .mapToLong(m -> ((Number) m.get("count")).longValue()).sum();
            long secondHalf = categoryTrends.subList(categoryTrends.size() / 2, categoryTrends.size()).stream()
                    .mapToLong(m -> ((Number) m.get("count")).longValue()).sum();
            result.put("trend", secondHalf > firstHalf ? "上升" : secondHalf < firstHalf ? "下降" : "稳定");
        } else {
            result.put("trend", "稳定");
        }

        log.info("获取借阅趋势: days={}, 分类数量={}", days, categoryTrends.size());
        return result;
    }

    @Override
    public Map<String, Object> getOverdueRisk(Integer readerId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("readerId", readerId);

        Map<String, Object> riskAssessment = new LinkedHashMap<>();

        // 当前借阅数
        int currentBorrowings = borrowingMapper.countByReaderIdAndStatus(readerId, 1);
        riskAssessment.put("currentBorrowings", currentBorrowings);

        // 逾期次数
        int overdueCount = borrowingMapper.countOverdueByReaderId(readerId);
        riskAssessment.put("overdueCount", overdueCount);

        // 即将到期的借阅（3天内）
        List<com.library.entity.Borrowing> activeBorrowings = borrowingMapper.findActiveByReaderId(readerId);
        int dueSoonCount = 0;
        for (com.library.entity.Borrowing b : activeBorrowings) {
            if (b.getDueDate() != null) {
                long daysToDue = ChronoUnit.DAYS.between(LocalDateTime.now(), b.getDueDate());
                if (daysToDue >= 0 && daysToDue <= 3) {
                    dueSoonCount++;
                }
            }
        }
        riskAssessment.put("dueSoonCount", dueSoonCount);

        // 计算风险分数（0-100）
        int riskScore = 0;
        riskScore += Math.min(overdueCount * 20, 40); // 逾期历史权重最高40分
        riskScore += Math.min(currentBorrowings * 5, 20); // 当前借阅量权重20分
        riskScore += Math.min(dueSoonCount * 15, 30); // 即将到期权重30分
        riskScore += (overdueCount > 0 ? 10 : 0); // 有逾期记录加10分

        riskScore = Math.min(riskScore, 100);

        // 风险等级
        String riskLevel;
        String suggestion;
        if (riskScore >= 60) {
            riskLevel = "高";
            suggestion = "存在较多逾期记录，请注意按时归还图书";
        } else if (riskScore >= 30) {
            riskLevel = "中";
            suggestion = "建议及时归还即将到期的图书";
        } else {
            riskLevel = "低";
            suggestion = "借阅记录良好，继续保持";
        }

        riskAssessment.put("riskScore", riskScore);
        riskAssessment.put("riskLevel", riskLevel);
        riskAssessment.put("suggestion", suggestion);
        result.put("riskAssessment", riskAssessment);

        log.info("评估逾期风险: readerId={}, riskScore={}, riskLevel={}", readerId, riskScore, riskLevel);
        return result;
    }

    @Override
    public Map<String, Object> getSeatHeatmap() {
        Map<String, Object> result = new LinkedHashMap<>();

        // 获取热力图数据：按星期几和小时统计签到次数
        List<Map<String, Object>> heatmapData = seatCheckinMapper.findHeatmapData();
        result.put("heatmapData", heatmapData);
        result.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 获取当前在座人数
        int currentlyCheckedIn = seatCheckinMapper.countTotalCheckedIn();
        result.put("currentlyCheckedIn", currentlyCheckedIn);

        log.info("获取座位热力图数据，共{}条记录", heatmapData.size());
        return result;
    }

    @Override
    public void cacheSeatPredictions() {
        log.info("开始缓存座位预测数据...");

        // 预缓存常用时段的预测
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
        String[] timeSlots = {"09:00-11:00", "11:00-13:00", "14:00-16:00", "16:00-18:00"};

        Map<String, Object> cache = new HashMap<>();
        for (String timeSlot : timeSlots) {
            cache.put("today_" + timeSlot, getSeatPrediction(today, timeSlot));
            cache.put("tomorrow_" + timeSlot, getSeatPrediction(tomorrow, timeSlot));
        }

        redisCacheService.set(SEAT_PREDICTION_KEY, cache, SEAT_PREDICTION_TTL_MINUTES);
        log.info("座位预测数据缓存完成（Redis），共{}条", cache.size());
    }

    /**
     * 解析时段字符串，返回[开始小时, 结束小时]
     */
    private int[] parseTimeSlot(String timeSlot) {
        if (timeSlot == null || !timeSlot.contains("-")) {
            return new int[]{9, 18}; // 默认全天
        }
        try {
            String[] parts = timeSlot.split("-");
            int startHour = Integer.parseInt(parts[0].split(":")[0]);
            int endHour = Integer.parseInt(parts[1].split(":")[0]);
            return new int[]{startHour, endHour};
        } catch (Exception e) {
            log.warn("解析时段失败: {}", timeSlot);
            return new int[]{9, 18};
        }
    }
}

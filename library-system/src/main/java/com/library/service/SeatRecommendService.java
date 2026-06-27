package com.library.service;

import java.util.List;
import java.util.Map;

/**
 * 座位推荐服务接口
 */
public interface SeatRecommendService {
    /**
     * 为读者推荐座位
     */
    List<Map<String, Object>> recommendSeats(Integer readerId, String date, String timeSlot, int limit);

    /**
     * 检测恶意取消预约行为
     */
    void detectMaliciousCancels();
}

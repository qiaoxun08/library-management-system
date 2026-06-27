package com.library.service;

import java.util.Map;

/**
 * 数据分析服务接口
 */
public interface AnalysisService {
    /**
     * 获取座位使用预测
     */
    Map<String, Object> getSeatPrediction(String date, String timeSlot);

    /**
     * 获取借阅趋势
     */
    Map<String, Object> getBookTrend(int days);

    /**
     * 获取读者逾期风险评估
     */
    Map<String, Object> getOverdueRisk(Integer readerId);

    /**
     * 获取座位热力图数据
     */
    Map<String, Object> getSeatHeatmap();

    /**
     * 缓存座位预测数据
     */
    void cacheSeatPredictions();
}

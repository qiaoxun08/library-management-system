package com.library.service;

import java.util.List;
import java.util.Map;

/**
 * 图书推荐服务接口
 */
public interface RecommendationService {
    /**
     * 根据读者偏好获取个性化推荐
     */
    List<Map<String, Object>> getRecommendations(String readerId, int limit);

    /**
     * 获取相似图书推荐
     */
    List<Map<String, Object>> getSimilarBooks(Integer bookId, int limit);

    /**
     * 更新读者偏好分类
     */
    void updateReaderPreferences(Integer readerId);

    /**
     * 更新热门图书缓存
     */
    void updatePopularBooksCache();
}

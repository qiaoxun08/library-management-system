package com.library.service.impl;

import com.library.entity.Book;
import com.library.entity.Reader;
import com.library.mapper.BookMapper;
import com.library.mapper.BookReviewMapper;
import com.library.mapper.BorrowingMapper;
import com.library.mapper.ReaderMapper;
import com.library.service.RecommendationService;
import com.library.service.RedisCacheService;
import com.library.service.SystemConfigService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 图书推荐服务实现
 */
@Service
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationServiceImpl.class);

    private static final String POPULAR_BOOKS_KEY = "popular_books";
    private static final long POPULAR_BOOKS_TTL_MINUTES = 26 * 60; // 26小时

    @Autowired
    private BorrowingMapper borrowingMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private ReaderMapper readerMapper;

    @Autowired
    private BookReviewMapper bookReviewMapper;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private RedisCacheService redisCacheService;

    @Override
    public List<Map<String, Object>> getRecommendations(String readerId, int limit) {
        Reader reader = readerMapper.findByReaderId(readerId);
        if (reader == null) {
            log.warn("读者不存在: readerId={}", readerId);
            return Collections.emptyList();
        }

        // 从 system_config 读取推荐权重（可在线调整，无需重启）
        double wBorrowHistory = getConfigDouble("library.recommend.weight_borrow_history", 0.4);
        double wPreferred = getConfigDouble("library.recommend.weight_preferred_categories", 0.3);
        double wHot = getConfigDouble("library.recommend.weight_hot_books", 0.2);
        double wPeer = getConfigDouble("library.recommend.weight_peer_rating", 0.1);

        // 获取读者偏好分类
        List<String> preferredCategories = parseCategories(reader.getPreferredCategories());

        // 获取已借过的图书ID和数量
        List<Integer> borrowedBookIds = borrowingMapper.findBorrowedBookIdsByReaderId(reader.getId());

        // 候选图书池：偏好分类中未借过的 + 全局热门
        List<Book> categoryBooks = preferredCategories.isEmpty()
                ? Collections.emptyList()
                : bookMapper.findAvailableByCategories(preferredCategories, borrowedBookIds);
        List<Book> popularBooks = borrowingMapper.findPopularBooks(limit * 3);

        // 合并候选集（去重）
        Map<Integer, Book> candidateMap = new LinkedHashMap<>();
        categoryBooks.forEach(b -> candidateMap.putIfAbsent(b.getId(), b));
        popularBooks.forEach(b -> candidateMap.putIfAbsent(b.getId(), b));

        // 批量查询候选图书的平均评分（peer_rating 维度）
        List<Integer> candidateIds = new ArrayList<>(candidateMap.keySet());
        Map<Integer, Map<String, Object>> bookRatings = new HashMap<>();
        if (!candidateIds.isEmpty()) {
            List<Map<String, Object>> ratings = bookReviewMapper.findAvgRatingByBookIds(candidateIds);
            for (Map<String, Object> r : ratings) {
                bookRatings.put((Integer) r.get("bookId"), r);
            }
        }

        // 获取读者各分类借阅次数（wBorrowHistory 维度）
        Map<String, Integer> categoryBorrowCounts = borrowingMapper.findCategoryCountsByReaderId(reader.getId())
                .stream()
                .collect(Collectors.toMap(
                        m -> (String) m.get("category"),
                        m -> ((Number) m.get("count")).intValue(),
                        (a, b) -> a + b
                ));
        int maxBorrowCount = categoryBorrowCounts.values().stream().mapToInt(Integer::intValue).max().orElse(1);

        // 计算推荐分数
        List<Map<String, Object>> scored = new ArrayList<>();
        for (Book book : candidateMap.values()) {
            double score = 0;
            StringBuilder reason = new StringBuilder();

            // 维度1: 偏好分类匹配（wPreferred）
            if (preferredCategories.contains(book.getCategory())) {
                score += wPreferred * 100;
                reason.append("偏好分类「").append(book.getCategory()).append("」");
            }

            // 维度2: 热门程度（wHot）—— 按候选池中的位置近似
            int hotIndex = popularBooks.indexOf(book);
            if (hotIndex >= 0) {
                double hotScore = Math.max(0, 1.0 - hotIndex / (double) Math.max(popularBooks.size(), 1));
                score += wHot * hotScore * 100;
                if (reason.length() > 0) reason.append("，");
                reason.append("热门图书");
            }

            // 维度3: 借阅历史相似度（wBorrowHistory）—— 基于该分类的借阅次数
            int borrowCount = categoryBorrowCounts.getOrDefault(book.getCategory(), 0);
            if (borrowCount > 0) {
                double historyScore = (double) borrowCount / maxBorrowCount;
                score += wBorrowHistory * historyScore * 100;
                if (reason.length() > 0) reason.append("，");
                reason.append("借阅过").append(book.getCategory()).append("类图书");
            }

            // 维度4: 同伴评分（wPeer_rating）—— 基于书评平均评分
            Map<String, Object> ratingInfo = bookRatings.get(book.getId());
            if (ratingInfo != null) {
                double avgRating = ((Number) ratingInfo.get("avgRating")).doubleValue();
                int reviewCount = ((Number) ratingInfo.get("reviewCount")).intValue();
                // 评分归一化到 0-100（1-5星 → 0-100）
                double peerScore = ((avgRating - 1.0) / 4.0) * 100;
                // 评论数越多，权重越高（最多1.5倍加成）
                double countBoost = Math.min(1.5, 1.0 + reviewCount * 0.1);
                score += wPeer * peerScore * countBoost;
                if (reason.length() > 0) reason.append("，");
                reason.append("评分").append(String.format("%.1f", avgRating)).append("分");
            }

            if (reason.length() == 0) {
                reason.append("综合推荐");
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", book.getId());
            item.put("title", book.getTitle());
            item.put("author", book.getAuthor());
            item.put("category", book.getCategory());
            item.put("coverUrl", book.getCoverUrl());
            item.put("availableCount", book.getAvailableCount());
            item.put("score", Math.round(score * 100) / 100.0);
            item.put("reason", reason.toString());
            scored.add(item);
        }

        // 按分数降序，取 top N
        scored.sort((a, b) -> Double.compare((double) b.get("score"), (double) a.get("score")));
        List<Map<String, Object>> recommendations = scored.stream().limit(limit).collect(Collectors.toList());

        // 如果推荐数量不足（候选池太小），补充热门图书兜底
        if (recommendations.size() < limit) {
            Set<Integer> existingIds = recommendations.stream()
                    .map(m -> (Integer) m.get("id"))
                    .collect(Collectors.toSet());
            List<Map<String, Object>> popular = getPopularBooks(limit - recommendations.size());
            for (Map<String, Object> pop : popular) {
                if (!existingIds.contains(pop.get("id"))) {
                    pop.put("reason", "热门图书");
                    recommendations.add(pop);
                }
            }
        }

        log.info("生成个性化推荐: readerId={}, 推荐数量={}, 权重=[偏好={},热门={},同伴={},历史={}]",
                readerId, recommendations.size(), wPreferred, wHot, wPeer, wBorrowHistory);
        return recommendations;
    }

    @Override
    public List<Map<String, Object>> getSimilarBooks(Integer bookId, int limit) {
        Book book = bookMapper.findById(bookId);
        if (book == null || book.getCategory() == null) {
            log.warn("图书不存在或无分类信息: bookId={}", bookId);
            return Collections.emptyList();
        }

        // 查找同分类的其他可借图书
        List<Book> sameCategoryBooks = bookMapper.findAvailableByCategory(book.getCategory());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Book b : sameCategoryBooks) {
            if (b.getId().equals(bookId)) {
                continue;
            }
            if (result.size() >= limit) {
                break;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", b.getId());
            item.put("title", b.getTitle());
            item.put("author", b.getAuthor());
            item.put("category", b.getCategory());
            item.put("coverUrl", b.getCoverUrl());
            item.put("availableCount", b.getAvailableCount());
            item.put("reason", "与《" + book.getTitle() + "》同属「" + book.getCategory() + "」分类");
            result.add(item);
        }

        log.info("获取相似图书推荐: bookId={}, 结果数量={}", bookId, result.size());
        return result;
    }

    @Override
    @Transactional
    public void updateReaderPreferences(Integer readerId) {
        Reader reader = readerMapper.findById(readerId);
        if (reader == null) {
            log.warn("读者不存在: readerId={}", readerId);
            return;
        }

        // 查询读者借阅历史，按分类统计
        List<Map<String, Object>> categoryCounts = borrowingMapper.findCategoryCountsByReaderId(readerId);

        if (categoryCounts.isEmpty()) {
            log.info("读者无借阅记录，跳过偏好更新: readerId={}", readerId);
            return;
        }

        // 取前5个偏好分类
        List<String> topCategories = categoryCounts.stream()
                .limit(5)
                .map(m -> (String) m.get("category"))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (topCategories.isEmpty()) {
            return;
        }

        // 序列化为JSON数组
        try {
            String categoriesJson = new ObjectMapper().writeValueAsString(topCategories);
            readerMapper.updatePreferredCategories(reader.getId(), categoriesJson);
            log.info("更新读者偏好分类: readerId={}, categories={}", readerId, categoriesJson);
        } catch (Exception e) {
            log.error("序列化偏好分类失败: readerId={}", readerId, e);
        }
    }

    @Override
    public void updatePopularBooksCache() {
        List<Map<String, Object>> popular = getPopularBooks(20);
        redisCacheService.set(POPULAR_BOOKS_KEY, popular, POPULAR_BOOKS_TTL_MINUTES);
        log.info("更新热门图书缓存（Redis），共{}本", popular.size());
    }

    /**
     * 获取热门图书列表（优先从缓存读取）
     */
    private List<Map<String, Object>> getPopularBooks(int limit) {
        // 优先从 Redis 缓存读取
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cached = redisCacheService.get(POPULAR_BOOKS_KEY, List.class);
        if (cached != null && !cached.isEmpty()) {
            return cached.stream().limit(limit).collect(Collectors.toList());
        }
        // 缓存未命中，查数据库
        List<Book> books = borrowingMapper.findPopularBooks(limit);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Book book : books) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", book.getId());
            item.put("title", book.getTitle());
            item.put("author", book.getAuthor());
            item.put("category", book.getCategory());
            item.put("coverUrl", book.getCoverUrl());
            item.put("availableCount", book.getAvailableCount());
            result.add(item);
        }
        return result;
    }

    /**
     * 获取带推荐理由的热门图书
     */
    private List<Map<String, Object>> getPopularBooksWithReason(int limit) {
        List<Map<String, Object>> books = getPopularBooks(limit);
        books.forEach(m -> m.put("reason", "热门图书"));
        return books;
    }

    /**
     * 从 SystemConfig 读取配置值，带默认值
     */
    private double getConfigDouble(String key, double defaultValue) {
        String value = systemConfigService.getConfigValue(key);
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return defaultValue;
    }

    /**
     * 解析偏好分类JSON数组
     */
    private List<String> parseCategories(String categoriesJson) {
        if (categoriesJson == null || categoriesJson.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return new ObjectMapper().readValue(categoriesJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("解析偏好分类失败: {}", categoriesJson, e);
            return Collections.emptyList();
        }
    }
}

package com.library.controller;

import com.library.dto.Result;
import com.library.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 图书推荐接口
 */
@RestController
@RequestMapping("/recommendation")
@Tag(name = "图书推荐", description = "个性化图书推荐、相似图书推荐相关接口")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    /**
     * 获取当前读者的个性化推荐（readerId 从 JWT 获取）
     */
    @GetMapping("/mine")
    @PreAuthorize("hasRole('READER')")
    @Operation(summary = "获取我的个性化推荐", description = "根据借阅历史获取个性化图书推荐")
    public Result<List<Map<String, Object>>> getMyRecommendations(
            @Parameter(description = "推荐数量") @RequestParam(defaultValue = "10") int limit) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String readerId = auth.getName();
            List<Map<String, Object>> recommendations = recommendationService.getRecommendations(readerId, limit);
            return Result.success(recommendations);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取读者的个性化推荐（管理员/馆员用）
     */
    @GetMapping("/{readerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "获取读者的个性化推荐", description = "管理员/馆员查看指定读者的推荐")
    public Result<List<Map<String, Object>>> getRecommendations(
            @Parameter(description = "读者编号") @PathVariable String readerId,
            @Parameter(description = "推荐数量") @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> recommendations = recommendationService.getRecommendations(readerId, limit);
            return Result.success(recommendations);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取相似图书（用于图书详情页）
     */
    @GetMapping("/books/{bookId}/similar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @Operation(summary = "获取相似图书推荐", description = "根据图书ID获取相似图书列表")
    public Result<List<Map<String, Object>>> getSimilarBooks(
            @Parameter(description = "图书ID") @PathVariable Integer bookId,
            @Parameter(description = "推荐数量") @RequestParam(defaultValue = "5") int limit) {
        try {
            List<Map<String, Object>> similarBooks = recommendationService.getSimilarBooks(bookId, limit);
            return Result.success(similarBooks);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

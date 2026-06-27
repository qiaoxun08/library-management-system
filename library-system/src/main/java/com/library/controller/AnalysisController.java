package com.library.controller;

import com.library.dto.Result;
import com.library.service.AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 数据分析接口
 */
@RestController
@RequestMapping("/analysis")
@Tag(name = "数据分析", description = "座位预测、借阅趋势、风险评估等数据分析接口")
public class AnalysisController {

    @Autowired
    private AnalysisService analysisService;

    /**
     * 座位预测
     */
    @GetMapping("/seat-prediction")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "座位使用预测", description = "根据日期和时段预测座位使用情况")
    public Result<Map<String, Object>> getSeatPrediction(
            @Parameter(description = "日期，如2026-06-27") @RequestParam String date,
            @Parameter(description = "时间段，如09:00-12:00") @RequestParam String timeSlot) {
        try {
            Map<String, Object> prediction = analysisService.getSeatPrediction(date, timeSlot);
            return Result.success(prediction);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 图书借阅趋势
     */
    @GetMapping("/book-trend")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "图书借阅趋势分析", description = "获取最近N天的借阅趋势数据")
    public Result<Map<String, Object>> getBookTrend(
            @Parameter(description = "统计天数") @RequestParam(defaultValue = "30") int days) {
        try {
            Map<String, Object> trend = analysisService.getBookTrend(days);
            return Result.success(trend);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 读者逾期风险评估
     */
    @GetMapping("/overdue-risk/{readerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "读者逾期风险评估", description = "评估指定读者的逾期风险等级")
    public Result<Map<String, Object>> getOverdueRisk(
            @Parameter(description = "读者ID") @PathVariable Integer readerId) {
        try {
            Map<String, Object> risk = analysisService.getOverdueRisk(readerId);
            return Result.success(risk);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 座位使用热力图
     */
    @GetMapping("/seat-heatmap")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "座位使用热力图", description = "获取座位使用热力图分析数据")
    public Result<Map<String, Object>> getSeatHeatmap() {
        try {
            Map<String, Object> heatmap = analysisService.getSeatHeatmap();
            return Result.success(heatmap);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

package com.library.controller;

import com.library.dto.Result;
import com.library.dto.StatisticsDTO;
import com.library.mapper.BorrowingMapper;
import com.library.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/statistics")
@Tag(name = "数据统计", description = "图书馆数据统计相关接口")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private BorrowingMapper borrowingMapper;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "获取仪表盘统计数据")
    public Result<StatisticsDTO> getDashboardStatistics() {
        try {
            StatisticsDTO statistics = statisticsService.getDashboardStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/books/category")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "按类别统计图书数量")
    public Result<Map<String, Integer>> getBooksByCategory() {
        try {
            Map<String, Integer> booksByCategory = statisticsService.getBooksByCategory();
            return Result.success(booksByCategory);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/borrowings/monthly")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "按月统计借阅量")
    public Result<List<Integer>> getBorrowingsByMonth() {
        try {
            List<Integer> borrowingsByMonth = statisticsService.getBorrowingsByMonth();
            return Result.success(borrowingsByMonth);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/reservations/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "按状态统计预约数量")
    public Result<Map<Integer, Integer>> getReservationsByStatus() {
        try {
            Map<Integer, Integer> reservationsByStatus = statisticsService.getReservationsByStatus();
            return Result.success(reservationsByStatus);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/online-count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "获取当前活跃读者数")
    public Result<Integer> getOnlineCount() {
        // 简单实现：返回当前活跃用户数（基于数据库中的正常读者数）
        return Result.success(statisticsService.getActiveReaderCount());
    }

    /**
     * 获取座位使用热力图
     */
    @GetMapping("/seat-heatmap")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "获取座位使用热力图数据")
    public Result<Map<String, Object>> getSeatHeatmap() {
        try {
            // 模拟热力图数据
            Map<String, Object> result = new java.util.LinkedHashMap<>();
            java.util.List<Map<String, Object>> heatmapData = new java.util.ArrayList<>();
            String[] areas = {"A区", "B区", "C区", "D区"};
            for (String area : areas) {
                Map<String, Object> areaData = new java.util.LinkedHashMap<>();
                areaData.put("area", area);
                areaData.put("utilization", (int) (Math.random() * 100));
                areaData.put("totalSeats", 20);
                areaData.put("occupiedSeats", (int) (Math.random() * 20));
                heatmapData.add(areaData);
            }
            result.put("heatmapData", heatmapData);
            result.put("lastUpdated", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 按月统计逾期率（最近12个月）
     */
    @GetMapping("/overdue-rate/monthly")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "按月统计逾期率", description = "最近12个月的逾期率趋势")
    public Result<List<Map<String, Object>>> getOverdueRateByMonth() {
        try {
            List<Map<String, Object>> data = borrowingMapper.countOverdueRateByMonth();
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 按院系统计借阅量分布
     */
    @GetMapping("/borrowings/department")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "按院系统计借阅量分布")
    public Result<List<Map<String, Object>>> getBorrowingsByDepartment() {
        try {
            List<Map<String, Object>> data = borrowingMapper.countByDepartment();
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

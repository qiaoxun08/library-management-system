package com.library.controller;

import com.library.annotation.OperLog;
import com.library.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 数据导出接口
 */
@RestController
@RequestMapping("/export")
@Tag(name = "数据导出", description = "借阅记录、逾期记录、排行等数据导出接口")
public class ExportController {

    @Autowired
    private ExportService exportService;

    /**
     * 导出借阅记录
     */
    @GetMapping("/borrowings")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @OperLog(module = "数据导出", action = "导出借阅记录")
    @Operation(summary = "导出借阅记录", description = "导出借阅记录为Excel/CSV文件")
    public void exportBorrowings(
            @Parameter(description = "开始日期，如2026-01-01") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期，如2026-06-27") @RequestParam(required = false) String endDate,
            @Parameter(description = "导出格式，xlsx或csv") @RequestParam(defaultValue = "xlsx") String format,
            HttpServletResponse response) throws Exception {
        exportService.exportBorrowings(startDate, endDate, format, response);
    }

    /**
     * 导出逾期记录
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @OperLog(module = "数据导出", action = "导出逾期记录")
    @Operation(summary = "导出逾期记录", description = "导出逾期记录为Excel/CSV文件")
    public void exportOverdue(
            @Parameter(description = "月份，如2026-06") @RequestParam(required = false) String month,
            @Parameter(description = "导出格式，xlsx或csv") @RequestParam(defaultValue = "xlsx") String format,
            HttpServletResponse response) throws Exception {
        exportService.exportOverdue(month, format, response);
    }

    /**
     * 导出读者积分排行
     */
    @GetMapping("/reader-ranking")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @OperLog(module = "数据导出", action = "导出积分排行")
    @Operation(summary = "导出读者积分排行", description = "导出读者积分排行榜为Excel/CSV文件")
    public void exportReaderRanking(
            @Parameter(description = "导出格式，xlsx或csv") @RequestParam(defaultValue = "xlsx") String format,
            HttpServletResponse response) throws Exception {
        exportService.exportReaderRanking(format, response);
    }

    /**
     * 导出操作日志
     */
    @GetMapping("/operation-logs")
    @PreAuthorize("hasRole('ADMIN')")
    @OperLog(module = "数据导出", action = "导出操作日志")
    @Operation(summary = "导出操作日志", description = "导出系统操作日志为Excel/CSV文件")
    public void exportOperationLogs(
            @Parameter(description = "导出格式，xlsx或csv") @RequestParam(defaultValue = "xlsx") String format,
            HttpServletResponse response) throws Exception {
        exportService.exportOperationLogs(format, response);
    }
}

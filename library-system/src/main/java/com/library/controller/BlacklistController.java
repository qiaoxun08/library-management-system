package com.library.controller;

import com.library.annotation.OperLog;
import com.library.dto.Result;
import com.library.entity.Blacklist;
import com.library.mapper.BorrowingMapper;
import com.library.service.BlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 黑名单管理接口
 */
@RestController
@RequestMapping("/blacklist")
@Tag(name = "黑名单管理", description = "读者黑名单的增删查及相关接口")
public class BlacklistController {

    @Autowired
    private BlacklistService blacklistService;

    @Autowired
    private BorrowingMapper borrowingMapper;

    /**
     * 获取所有黑名单记录
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取所有黑名单记录")
    public Result<List<Blacklist>> getAllBlacklist() {
        List<Blacklist> blacklist = blacklistService.getAllBlacklist();
        return Result.success(blacklist);
    }

    /**
     * 获取当前生效的黑名单
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "获取当前生效的黑名单")
    public Result<List<Blacklist>> getActiveBlacklist() {
        List<Blacklist> blacklist = blacklistService.getActiveBlacklist();
        return Result.success(blacklist);
    }

    /**
     * 检查读者是否在黑名单中
     */
    @GetMapping("/check/{readerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @Operation(summary = "检查读者是否在黑名单中")
    public Result<Boolean> checkBlacklist(
            @Parameter(description = "读者ID") @PathVariable Integer readerId) {
        boolean isBlacklisted = blacklistService.isBlacklisted(readerId);
        return Result.success(isBlacklisted);
    }

    /**
     * 添加黑名单
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @OperLog(module = "黑名单", action = "添加黑名单")
    @Operation(summary = "添加黑名单", description = "将读者加入黑名单")
    public Result<Blacklist> addToBlacklist(@RequestBody Blacklist blacklist) {
        Blacklist result = blacklistService.addToBlacklist(blacklist);
        return Result.success(result);
    }

    /**
     * 从黑名单移除
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperLog(module = "黑名单", action = "移除黑名单")
    @Operation(summary = "从黑名单移除", description = "将读者从黑名单中移除")
    public Result<Void> removeFromBlacklist(
            @Parameter(description = "黑名单记录ID") @PathVariable Integer id) {
        blacklistService.removeFromBlacklist(id);
        return Result.success();
    }

    /**
     * 获取读者的违约明细（超时未签到/取消预约）
     */
    @GetMapping("/violations/{readerId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取读者的违约明细", description = "查看读者的超时未签到/取消预约等违约记录")
    public Result<List<Map<String, Object>>> getViolationDetails(
            @Parameter(description = "读者ID") @PathVariable Integer readerId) {
        List<Map<String, Object>> details = borrowingMapper.findViolationDetails(readerId);
        return Result.success(details);
    }
}

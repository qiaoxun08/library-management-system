package com.library.controller;

import com.library.dto.Result;
import com.library.entity.LoginLog;
import com.library.mapper.LoginLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 登录审计日志控制器
 */
@RestController
@RequestMapping("/login-logs")
@Tag(name = "登录日志", description = "登录审计日志查询相关接口")
public class LoginLogController {

    @Autowired
    private LoginLogMapper loginLogMapper;

    /**
     * 分页获取登录日志（仅管理员）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "分页获取登录日志")
    public Result<Map<String, Object>> getLoginLogs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int size) {
        int offset = (page - 1) * size;
        List<LoginLog> records = loginLogMapper.findPaged(offset, size);
        int total = loginLogMapper.countAll();
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return Result.success(result);
    }
}

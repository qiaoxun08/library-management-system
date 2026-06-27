package com.library.controller;

import com.library.annotation.OperLog;
import com.library.dto.Result;
import com.library.dto.SystemConfigUpdateRequest;
import com.library.entity.SystemConfig;
import com.library.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/config")
@Tag(name = "系统配置", description = "系统参数配置管理相关接口")
public class SystemConfigController {

    @Autowired
    private SystemConfigService systemConfigService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取所有配置项")
    public Result<List<SystemConfig>> getAllConfig() {
        List<SystemConfig> configs = systemConfigService.getAllConfig();
        return Result.success(configs);
    }

    @GetMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "根据键获取配置项")
    public Result<SystemConfig> getConfigByKey(
            @Parameter(description = "配置键") @PathVariable String key) {
        SystemConfig config = systemConfigService.getConfigByKey(key);
        return Result.success(config);
    }

    /**
     * 批量更新配置（前端发送扁平的 key-value 对象）
     * 例如: { "library.borrowing.default-days": "30", "library.fine.daily-rate": "0.10" }
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @OperLog(module = "系统配置", action = "更新参数")
    @Operation(summary = "批量更新配置", description = "批量更新系统配置参数，前端发送key-value对象")
    public Result<Void> batchUpdateConfig(@Valid @RequestBody SystemConfigUpdateRequest request) {
        for (java.util.Map.Entry<String, String> entry : request.getConfigs().entrySet()) {
            systemConfigService.updateConfigByKey(entry.getKey(), entry.getValue());
        }
        return Result.success();
    }
}

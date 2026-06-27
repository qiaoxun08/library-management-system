package com.library.controller;

import com.library.dto.Result;
import com.library.entity.OperationLog;
import com.library.mapper.OperationLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/logs")
@Tag(name = "操作日志", description = "系统操作日志查询相关接口")
public class OperationLogController {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "分页获取操作日志")
    public Result<Map<String, Object>> getAllLogs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int size) {
        int offset = (page - 1) * size;
        List<OperationLog> records = operationLogMapper.findPaged(offset, size);
        int total = operationLogMapper.countAll();
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return Result.success(result);
    }
}

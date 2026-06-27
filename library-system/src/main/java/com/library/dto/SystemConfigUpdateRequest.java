package com.library.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统配置批量更新请求 DTO
 * 接受扁平的 key-value 格式：{"library.borrowing.default-days": "30", ...}
 */
@Data
public class SystemConfigUpdateRequest {

    private Map<String, String> configs = new HashMap<>();

    @JsonAnySetter
    public void setConfig(String key, String value) {
        this.configs.put(key, value);
    }

    /**
     * 校验 configs 非空
     */
    @NotEmpty(message = "配置项不能为空")
    public Map<String, String> getConfigs() {
        return configs;
    }
}

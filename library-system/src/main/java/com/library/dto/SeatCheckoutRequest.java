package com.library.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 座位签退请求 DTO
 */
@Data
public class SeatCheckoutRequest {

    @NotNull(message = "签到记录ID不能为空")
    private Integer checkinId;
}

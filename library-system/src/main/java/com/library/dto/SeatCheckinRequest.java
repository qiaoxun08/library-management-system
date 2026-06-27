package com.library.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 座位签到请求 DTO
 */
@Data
public class SeatCheckinRequest {

    @NotNull(message = "座位ID不能为空")
    private Integer seatId;

    @NotNull(message = "预约ID不能为空")
    private Integer reservationId;

    /**
     * 管理员/馆员场景下可指定 readerId，READER 角色从 JWT 获取
     */
    private Integer readerId;
}

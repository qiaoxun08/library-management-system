package com.library.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 关注读者请求 DTO
 */
@Data
public class FollowRequest {

    /**
     * 管理员/馆员场景下可指定 followerId，READER 角色从 JWT 获取
     */
    private Integer followerId;

    @NotNull(message = "被关注者ID不能为空")
    private Integer followeeId;
}

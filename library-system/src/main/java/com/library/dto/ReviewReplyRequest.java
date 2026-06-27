package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发表书评回复请求 DTO
 */
@Data
public class ReviewReplyRequest {

    @NotBlank(message = "回复内容不能为空")
    @Size(max = 500, message = "回复内容不能超过500个字符")
    private String content;

    /**
     * 被回复者ID（楼中楼回复时使用，一级回复为 null）
     */
    private Integer replyToReaderId;
}

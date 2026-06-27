package com.library.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 添加书评请求 DTO
 */
@Data
public class BookReviewAddRequest {

    @NotNull(message = "图书ID不能为空")
    private Integer bookId;

    /**
     * 管理员/馆员场景下可指定 readerId，READER 角色从 JWT 获取
     */
    private Integer readerId;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容不能超过1000个字符")
    private String content;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为1")
    @Max(value = 5, message = "评分最高为5")
    private Integer rating;
}

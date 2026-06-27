package com.library.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 书评回复实体
 */
@Data
public class ReviewReply {
    private Integer id;
    private Integer reviewId;
    private Integer readerId;
    private String content;
    private Integer replyToReaderId;
    private Integer status;
    private LocalDateTime createTime;

    // 非数据库字段：回复者姓名
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String readerName;

    // 非数据库字段：被回复者姓名
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String replyToReaderName;
}

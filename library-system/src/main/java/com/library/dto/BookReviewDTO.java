package com.library.dto;

import java.time.LocalDateTime;

/**
 * 图书评论DTO
 */
public class BookReviewDTO {
    private Integer id;
    private Integer bookId;
    private Integer readerId;
    private String readerName;
    private String content;
    private Integer rating;
    private Integer likeCount;
    private Boolean hasLiked;
    private LocalDateTime createTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getBookId() { return bookId; }
    public void setBookId(Integer bookId) { this.bookId = bookId; }

    public Integer getReaderId() { return readerId; }
    public void setReaderId(Integer readerId) { this.readerId = readerId; }

    public String getReaderName() { return readerName; }
    public void setReaderName(String readerName) { this.readerName = readerName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public Boolean getHasLiked() { return hasLiked; }
    public void setHasLiked(Boolean hasLiked) { this.hasLiked = hasLiked; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}

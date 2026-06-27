package com.library.dto;

/**
 * 图书推荐DTO
 */
public class RecommendationDTO {
    private Integer bookId;
    private String title;
    private String author;
    private String category;
    private String coverUrl;
    private Integer availableCount;
    private String reason;

    public Integer getBookId() { return bookId; }
    public void setBookId(Integer bookId) { this.bookId = bookId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public Integer getAvailableCount() { return availableCount; }
    public void setAvailableCount(Integer availableCount) { this.availableCount = availableCount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

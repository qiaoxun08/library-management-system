package com.library.entity;

import java.time.LocalDateTime;

/**
 * 读者等级积分实体
 */
public class ReaderLevel {
    private Integer id;
    private Integer readerId;
    private Integer points;
    private String level;
    private Integer totalBorrowCount;
    private Integer totalReturnOnTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getReaderId() { return readerId; }
    public void setReaderId(Integer readerId) { this.readerId = readerId; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public Integer getTotalBorrowCount() { return totalBorrowCount; }
    public void setTotalBorrowCount(Integer totalBorrowCount) { this.totalBorrowCount = totalBorrowCount; }
    public Integer getTotalReturnOnTime() { return totalReturnOnTime; }
    public void setTotalReturnOnTime(Integer totalReturnOnTime) { this.totalReturnOnTime = totalReturnOnTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}

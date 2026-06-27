package com.library.entity;

import java.time.LocalDateTime;

/**
 * 学习搭档实体
 */
public class StudyBuddy {
    private Integer id;
    private Integer readerId;
    private String preferredSlot;
    private String preferredArea;
    private String studyGoal;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getReaderId() { return readerId; }
    public void setReaderId(Integer readerId) { this.readerId = readerId; }
    public String getPreferredSlot() { return preferredSlot; }
    public void setPreferredSlot(String preferredSlot) { this.preferredSlot = preferredSlot; }
    public String getPreferredArea() { return preferredArea; }
    public void setPreferredArea(String preferredArea) { this.preferredArea = preferredArea; }
    public String getStudyGoal() { return studyGoal; }
    public void setStudyGoal(String studyGoal) { this.studyGoal = studyGoal; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}

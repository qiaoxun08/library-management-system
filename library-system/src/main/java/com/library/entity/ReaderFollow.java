package com.library.entity;

import java.time.LocalDateTime;

/**
 * 读者关注关系实体
 */
public class ReaderFollow {
    private Integer id;
    private Integer followerId; // 关注者
    private Integer followeeId; // 被关注者
    private LocalDateTime createTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getFollowerId() { return followerId; }
    public void setFollowerId(Integer followerId) { this.followerId = followerId; }
    public Integer getFolloweeId() { return followeeId; }
    public void setFolloweeId(Integer followeeId) { this.followeeId = followeeId; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}

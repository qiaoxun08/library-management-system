package com.library.service;

import java.util.Map;

/**
 * 读者关注服务接口
 */
public interface ReaderFollowService {
    /**
     * 关注读者
     */
    void follow(Integer followerId, Integer followeeId);

    /**
     * 取消关注
     */
    void unfollow(Integer followerId, Integer followeeId);

    /**
     * 检查是否已关注
     */
    boolean isFollowing(Integer followerId, Integer followeeId);

    /**
     * 获取粉丝列表
     */
    Map<String, Object> getFollowers(Integer readerId);

    /**
     * 获取关注列表
     */
    Map<String, Object> getFollowees(Integer readerId);
}

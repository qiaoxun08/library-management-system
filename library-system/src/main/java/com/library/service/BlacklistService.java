package com.library.service;

import com.library.entity.Blacklist;

import java.util.List;

/**
 * 黑名单服务接口
 */
public interface BlacklistService {
    List<Blacklist> getAllBlacklist();
    Blacklist getBlacklistById(Integer id);
    List<Blacklist> getActiveBlacklist();
    boolean isBlacklisted(Integer readerId);
    Blacklist addToBlacklist(Blacklist blacklist);
    void removeFromBlacklist(Integer id);
    void incrementViolation(Integer readerId);
}

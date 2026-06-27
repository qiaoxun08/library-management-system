package com.library.service;

import com.library.entity.ReaderLevel;

import java.util.List;

/**
 * 读者等级积分服务接口
 */
public interface ReaderLevelService {
    List<ReaderLevel> getAllReaderLevels();
    ReaderLevel getReaderLevel(Integer readerId);
    void initReaderLevel(Integer readerId);
    void addPoints(Integer readerId, int points);
    void subtractPoints(Integer readerId, int points);
    void incrementBorrowCount(Integer readerId);
    void incrementReturnOnTime(Integer readerId);
    void updateLevel(Integer readerId);
}

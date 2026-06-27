package com.library.service.impl;

import com.library.entity.ReaderLevel;
import com.library.mapper.ReaderLevelMapper;
import com.library.service.ReaderLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 读者等级积分服务实现
 */
@Service
public class ReaderLevelServiceImpl implements ReaderLevelService {

    @Autowired
    private ReaderLevelMapper readerLevelMapper;

    @Override
    public List<ReaderLevel> getAllReaderLevels() {
        return readerLevelMapper.findAll();
    }

    @Override
    public ReaderLevel getReaderLevel(Integer readerId) {
        return readerLevelMapper.findByReaderId(readerId);
    }

    @Override
    public void initReaderLevel(Integer readerId) {
        // 检查是否已初始化
        ReaderLevel existing = readerLevelMapper.findByReaderId(readerId);
        if (existing != null) {
            return;
        }
        ReaderLevel readerLevel = new ReaderLevel();
        readerLevel.setReaderId(readerId);
        readerLevel.setPoints(0);
        readerLevel.setLevel("普通");
        readerLevel.setTotalBorrowCount(0);
        readerLevel.setTotalReturnOnTime(0);
        readerLevelMapper.insert(readerLevel);
    }

    @Override
    public void addPoints(Integer readerId, int points) {
        readerLevelMapper.addPoints(readerId, points);
        updateLevel(readerId);
    }

    @Override
    public void subtractPoints(Integer readerId, int points) {
        readerLevelMapper.subtractPoints(readerId, points);
        updateLevel(readerId);
    }

    @Override
    public void incrementBorrowCount(Integer readerId) {
        readerLevelMapper.incrementBorrowCount(readerId);
    }

    @Override
    public void incrementReturnOnTime(Integer readerId) {
        readerLevelMapper.incrementReturnOnTime(readerId);
    }

    @Override
    public void updateLevel(Integer readerId) {
        ReaderLevel readerLevel = readerLevelMapper.findByReaderId(readerId);
        if (readerLevel == null) {
            return;
        }
        // 根据积分自动升级
        String level;
        if (readerLevel.getPoints() >= 1000) {
            level = "钻石";
        } else if (readerLevel.getPoints() >= 500) {
            level = "金牌";
        } else if (readerLevel.getPoints() >= 200) {
            level = "银牌";
        } else {
            level = "普通";
        }
        readerLevel.setLevel(level);
        readerLevelMapper.update(readerLevel);
    }
}

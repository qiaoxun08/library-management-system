package com.library.service.impl;

import com.library.entity.ReaderLevel;
import com.library.mapper.ReaderLevelMapper;
import com.library.service.ReaderLevelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 读者等级积分服务实现
 */
@Service
public class ReaderLevelServiceImpl implements ReaderLevelService {

    private static final Logger log = LoggerFactory.getLogger(ReaderLevelServiceImpl.class);

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
    @Transactional
    public void addPoints(Integer readerId, int points) {
        // 悲观锁：锁定行，防止并发修改导致积分丢失
        ReaderLevel readerLevel = readerLevelMapper.findByReaderIdForUpdate(readerId);
        if (readerLevel == null) {
            log.warn("addPoints: 读者积分记录不存在, readerId={}", readerId);
            return;
        }
        // 在锁保护下计算新积分
        int newPoints = readerLevel.getPoints() + points;
        readerLevel.setPoints(newPoints);
        // 根据积分更新等级
        updateLevelByPoints(readerLevel);
        readerLevelMapper.update(readerLevel);
        log.debug("addPoints: readerId={}, +{}分, 新积分={}", readerId, points, newPoints);
    }

    @Override
    @Transactional
    public void subtractPoints(Integer readerId, int points) {
        // 悲观锁：锁定行，防止并发修改导致积分丢失
        ReaderLevel readerLevel = readerLevelMapper.findByReaderIdForUpdate(readerId);
        if (readerLevel == null) {
            log.warn("subtractPoints: 读者积分记录不存在, readerId={}", readerId);
            return;
        }
        // 在锁保护下计算新积分（最小为0）
        int newPoints = Math.max(readerLevel.getPoints() - points, 0);
        readerLevel.setPoints(newPoints);
        // 根据积分更新等级
        updateLevelByPoints(readerLevel);
        readerLevelMapper.update(readerLevel);
        log.debug("subtractPoints: readerId={}, -{}分, 新积分={}", readerId, points, newPoints);
    }

    /**
     * 根据积分更新等级（内部方法，不加锁）
     */
    private void updateLevelByPoints(ReaderLevel readerLevel) {
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

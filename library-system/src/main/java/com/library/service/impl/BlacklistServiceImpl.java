package com.library.service.impl;

import com.library.entity.Blacklist;
import com.library.exception.BusinessException;
import com.library.mapper.BlacklistMapper;
import com.library.mapper.ReaderMapper;
import com.library.entity.Reader;
import com.library.service.BlacklistService;
import com.library.service.SystemConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 黑名单服务实现
 */
@Service
public class BlacklistServiceImpl implements BlacklistService {

    private static final Logger log = LoggerFactory.getLogger(BlacklistServiceImpl.class);

    @Autowired
    private BlacklistMapper blacklistMapper;

    @Autowired
    private ReaderMapper readerMapper;

    @Autowired
    private SystemConfigService systemConfigService;

    @Override
    public List<Blacklist> getAllBlacklist() {
        return blacklistMapper.findAll();
    }

    @Override
    public Blacklist getBlacklistById(Integer id) {
        Blacklist blacklist = blacklistMapper.findById(id);
        if (blacklist == null) {
            throw new BusinessException("黑名单记录不存在");
        }
        return blacklist;
    }

    @Override
    public List<Blacklist> getActiveBlacklist() {
        return blacklistMapper.findActiveBlacklist();
    }

    @Override
    public boolean isBlacklisted(Integer readerId) {
        Blacklist blacklist = blacklistMapper.findByReaderId(readerId);
        if (blacklist == null || blacklist.getBlacklisted() != 1) {
            return false;
        }
        // 检查是否已过期
        if (blacklist.getEndTime() != null && blacklist.getEndTime().isBefore(LocalDateTime.now())) {
            return false;
        }
        return true;
    }

    @Override
    @Transactional
    public Blacklist addToBlacklist(Blacklist blacklist) {
        // 检查读者是否存在
        Reader reader = readerMapper.findById(blacklist.getReaderId());
        if (reader == null) {
            throw new BusinessException("读者不存在");
        }

        // 检查是否已在黑名单中
        Blacklist existing = blacklistMapper.findByReaderId(blacklist.getReaderId());
        if (existing != null && existing.getBlacklisted() == 1) {
            // 检查是否已过期
            if (existing.getEndTime() == null || existing.getEndTime().isAfter(LocalDateTime.now())) {
                throw new BusinessException("该读者已在黑名单中");
            }
        }

        blacklist.setBlacklisted(1);
        blacklistMapper.insert(blacklist);

        // 禁用读者账号
        readerMapper.updateStatus(reader.getId(), 0);

        return blacklist;
    }

    @Override
    @Transactional
    public void removeFromBlacklist(Integer id) {
        Blacklist blacklist = blacklistMapper.findById(id);
        if (blacklist == null) {
            throw new BusinessException("黑名单记录不存在");
        }

        blacklist.setBlacklisted(0);
        blacklistMapper.update(blacklist);

        // 恢复读者账号
        Reader reader = readerMapper.findById(blacklist.getReaderId());
        if (reader != null) {
            readerMapper.updateStatus(reader.getId(), 1);
        }
    }

    @Override
    public void incrementViolation(Integer readerId) {
        blacklistMapper.incrementViolationCount(readerId);

        // 检查违规次数是否达到阈值，自动加入黑名单
        Blacklist existing = blacklistMapper.findByReaderId(readerId);
        if (existing != null) {
            String thresholdStr = systemConfigService.getConfigValue("library.blacklist.violation-threshold");
            int threshold = thresholdStr != null ? Integer.parseInt(thresholdStr) : 3;
            if (existing.getViolationCount() != null && existing.getViolationCount() >= threshold) {
                if (existing.getBlacklisted() == null || existing.getBlacklisted() != 1
                        || (existing.getEndTime() != null && existing.getEndTime().isBefore(LocalDateTime.now()))) {
                    log.info("读者违规次数达到阈值，自动加入黑名单: readerId={}, violationCount={}", readerId, existing.getViolationCount());
                    Blacklist blacklist = new Blacklist();
                    blacklist.setReaderId(readerId);
                    blacklist.setReason("违规次数达到" + threshold + "次，自动加入黑名单");
                    blacklist.setViolationCount(existing.getViolationCount());
                    blacklist.setBlacklisted(1);
                    blacklist.setStartTime(LocalDateTime.now());
                    // 黑名单有效期30天
                    blacklist.setEndTime(LocalDateTime.now().plusDays(30));
                    addToBlacklist(blacklist);
                }
            }
        }
    }
}

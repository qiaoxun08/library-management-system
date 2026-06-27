package com.library.service.impl;

import com.library.entity.StudyBuddy;
import com.library.exception.BusinessException;
import com.library.mapper.StudyBuddyMapper;
import com.library.service.StudyBuddyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学习搭档服务实现类
 */
@Service
public class StudyBuddyServiceImpl implements StudyBuddyService {

    private static final Logger log = LoggerFactory.getLogger(StudyBuddyServiceImpl.class);

    @Autowired
    private StudyBuddyMapper studyBuddyMapper;

    @Override
    public StudyBuddy getMyProfile(Integer readerId) {
        log.info("获取读者{}的学习搭档profile", readerId);
        return studyBuddyMapper.findByReaderId(readerId);
    }

    @Override
    @Transactional
    public void saveProfile(StudyBuddy profile) {
        log.info("保存读者{}的学习搭档profile", profile.getReaderId());

        // 检查是否已有profile
        StudyBuddy existing = studyBuddyMapper.findByReaderId(profile.getReaderId());

        if (existing != null) {
            // 更新现有profile
            profile.setReaderId(existing.getReaderId());
            profile.setCreateTime(existing.getCreateTime());
            profile.setUpdateTime(LocalDateTime.now());
            studyBuddyMapper.update(profile);
            log.info("更新读者{}的学习搭档profile", profile.getReaderId());
        } else {
            // 插入新profile
            profile.setCreateTime(LocalDateTime.now());
            profile.setUpdateTime(LocalDateTime.now());
            studyBuddyMapper.insert(profile);
            log.info("创建读者{}的学习搭档profile", profile.getReaderId());
        }
    }

    @Override
    public List<StudyBuddy> findMatchedBuddies(Integer readerId) {
        log.info("查找读者{}的学习搭档匹配", readerId);

        // 先查自己的profile
        StudyBuddy myProfile = studyBuddyMapper.findByReaderId(readerId);
        if (myProfile == null) {
            log.warn("读者{}没有学习搭档profile", readerId);
            return List.of();
        }

        // 查所有status=1的开放匹配
        List<StudyBuddy> openBuddies = studyBuddyMapper.findOpenBuddies();

        // 过滤：偏好区域相同且排除自己
        String myPreferredArea = myProfile.getPreferredArea();
        return openBuddies.stream()
                .filter(buddy -> !buddy.getReaderId().equals(readerId))  // 排除自己
                .filter(buddy -> myPreferredArea != null && myPreferredArea.equals(buddy.getPreferredArea()))  // 偏好区域相同
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteProfile(Integer readerId) {
        log.info("删除读者{}的学习搭档profile", readerId);
        StudyBuddy existing = studyBuddyMapper.findByReaderId(readerId);
        if (existing == null) {
            throw new BusinessException("没有找到学习搭档profile");
        }
        studyBuddyMapper.deleteByReaderId(readerId);
        log.info("成功删除读者{}的学习搭档profile", readerId);
    }
}
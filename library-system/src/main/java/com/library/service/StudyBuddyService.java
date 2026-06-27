package com.library.service;

import com.library.entity.StudyBuddy;
import java.util.List;

/**
 * 学习搭档服务接口
 */
public interface StudyBuddyService {
    /**
     * 获取当前读者的buddy profile
     */
    StudyBuddy getMyProfile(Integer readerId);

    /**
     * 保存/更新当前读者的buddy profile（upsert）
     */
    void saveProfile(StudyBuddy profile);

    /**
     * 查找与自己偏好区域相同且status=1的其他读者
     */
    List<StudyBuddy> findMatchedBuddies(Integer readerId);

    /**
     * 删除当前读者的buddy profile
     */
    void deleteProfile(Integer readerId);
}
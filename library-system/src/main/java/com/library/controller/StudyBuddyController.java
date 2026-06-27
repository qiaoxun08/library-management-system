package com.library.controller;

import com.library.dto.Result;
import com.library.entity.StudyBuddy;
import com.library.service.ReaderService;
import com.library.service.StudyBuddyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学习搭档控制器
 */
@RestController
@RequestMapping("/study-buddy")
@PreAuthorize("hasRole('READER')")
@Tag(name = "学习伙伴", description = "学习伙伴匹配与管理相关接口")
public class StudyBuddyController {

    @Autowired
    private StudyBuddyService studyBuddyService;

    @Autowired
    private ReaderService readerService;

    /**
     * 获取当前读者的buddy profile
     */
    @GetMapping("/mine")
    @Operation(summary = "获取我的学习伙伴资料")
    public Result<StudyBuddy> getMyProfile() {
        try {
            Integer readerId = resolveCurrentReaderId();
            StudyBuddy profile = studyBuddyService.getMyProfile(readerId);
            return Result.success(profile);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 保存/更新当前读者的buddy profile
     */
    @PostMapping
    @Operation(summary = "保存学习伙伴资料", description = "创建或更新自己的学习伙伴资料")
    public Result<Void> saveProfile(@RequestBody StudyBuddy profile) {
        try {
            Integer readerId = resolveCurrentReaderId();
            profile.setReaderId(readerId);
            studyBuddyService.saveProfile(profile);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取匹配的学习伙伴
     */
    @GetMapping("/match")
    @Operation(summary = "获取匹配的学习伙伴", description = "根据学习习惯匹配合适的学习伙伴")
    public Result<List<StudyBuddy>> getMatchedBuddies() {
        try {
            Integer readerId = resolveCurrentReaderId();
            List<StudyBuddy> buddies = studyBuddyService.findMatchedBuddies(readerId);
            return Result.success(buddies);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除当前读者的buddy profile
     */
    @DeleteMapping("/mine")
    @Operation(summary = "删除我的学习伙伴资料")
    public Result<Void> deleteProfile() {
        try {
            Integer readerId = resolveCurrentReaderId();
            studyBuddyService.deleteProfile(readerId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 从SecurityContext中解析当前读者的ID
     */
    private Integer resolveCurrentReaderId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String readerIdStr = auth.getName();
        // 通过readerId字符串查询Reader对象，获取数据库中的id
        var reader = readerService.getReaderByReaderId(readerIdStr);
        if (reader == null) {
            throw new RuntimeException("读者不存在");
        }
        return reader.getId();
    }
}

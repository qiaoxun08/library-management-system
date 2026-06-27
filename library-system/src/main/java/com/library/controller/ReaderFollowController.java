package com.library.controller;

import com.library.annotation.OperLog;
import com.library.dto.FollowRequest;
import com.library.dto.Result;
import com.library.entity.Reader;
import com.library.service.ReaderFollowService;
import com.library.service.ReaderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 读者关注接口
 */
@RestController
@RequestMapping("/follows")
@Tag(name = "关注管理", description = "读者互相关注、粉丝列表相关接口")
public class ReaderFollowController {

    @Autowired
    private ReaderFollowService readerFollowService;

    @Autowired
    private ReaderService readerService;

    /**
     * 关注读者（followerId 从 JWT 获取，不信前端传入）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @OperLog(module = "关注管理", action = "关注读者")
    @Operation(summary = "关注读者", description = "关注指定读者，READER角色自动使用当前用户身份")
    public Result<Void> follow(@Valid @RequestBody FollowRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isReader = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_READER"));
            // READER：followerId 从 JWT 取；ADMIN/LIBRARIAN：可指定
            Integer followerId = isReader ? resolveReaderIdFromAuth(auth.getName()) : request.getFollowerId();
            readerFollowService.follow(followerId, request.getFolloweeId());
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消关注（followerId 从 JWT 获取）
     */
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @OperLog(module = "关注管理", action = "取消关注")
    @Operation(summary = "取消关注", description = "取消对指定读者的关注")
    public Result<Void> unfollow(
            @Parameter(description = "被关注者ID") @RequestParam Integer followeeId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isReader = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_READER"));
            Integer followerId = isReader ? resolveReaderIdFromAuth(auth.getName()) : null;
            readerFollowService.unfollow(followerId, followeeId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查是否已关注（followerId 从 JWT 获取）
     */
    @GetMapping("/check")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @Operation(summary = "检查是否已关注", description = "检查当前用户是否已关注指定读者")
    public Result<Boolean> isFollowing(
            @Parameter(description = "被关注者ID") @RequestParam Integer followeeId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isReader = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_READER"));
            Integer followerId = isReader ? resolveReaderIdFromAuth(auth.getName()) : null;
            boolean following = readerFollowService.isFollowing(followerId, followeeId);
            return Result.success(following);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前读者的粉丝列表（readerId 从 JWT 获取）
     */
    @GetMapping("/followers/mine")
    @PreAuthorize("hasRole('READER')")
    @Operation(summary = "获取我的粉丝列表")
    public Result<Map<String, Object>> getMyFollowers() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Integer readerId = resolveReaderIdFromAuth(auth.getName());
            Map<String, Object> followers = readerFollowService.getFollowers(readerId);
            return Result.success(followers);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前读者的关注列表（readerId 从 JWT 获取）
     */
    @GetMapping("/followees/mine")
    @PreAuthorize("hasRole('READER')")
    @Operation(summary = "获取我的关注列表")
    public Result<Map<String, Object>> getMyFollowees() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Integer readerId = resolveReaderIdFromAuth(auth.getName());
            Map<String, Object> followees = readerFollowService.getFollowees(readerId);
            return Result.success(followees);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取粉丝列表（管理员/馆员用）
     */
    @GetMapping("/followers/{readerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "获取读者的粉丝列表", description = "管理员/馆员查看指定读者的粉丝")
    public Result<Map<String, Object>> getFollowers(
            @Parameter(description = "读者ID") @PathVariable Integer readerId) {
        try {
            Map<String, Object> followers = readerFollowService.getFollowers(readerId);
            return Result.success(followers);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取关注列表（管理员/馆员用）
     */
    @GetMapping("/followees/{readerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "获取读者的关注列表", description = "管理员/馆员查看指定读者的关注列表")
    public Result<Map<String, Object>> getFollowees(
            @Parameter(description = "读者ID") @PathVariable Integer readerId) {
        try {
            Map<String, Object> followees = readerFollowService.getFollowees(readerId);
            return Result.success(followees);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 从 JWT principal（readerId 字符串）解析为数据库 id
     */
    private Integer resolveReaderIdFromAuth(String readerIdStr) {
        Reader reader = readerService.getReaderByReaderId(readerIdStr);
        return reader != null ? reader.getId() : null;
    }
}

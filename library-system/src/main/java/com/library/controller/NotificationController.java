package com.library.controller;

import com.library.dto.Result;
import com.library.entity.Notification;
import com.library.entity.Reader;
import com.library.service.NotificationService;
import com.library.service.ReaderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知消息接口
 */
@RestController
@RequestMapping("/notifications")
@Tag(name = "通知管理", description = "通知消息查询、已读标记相关接口")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ReaderService readerService;

    /**
     * 获取当前读者的通知列表（readerId 从 JWT 获取）
     */
    @GetMapping("/mine")
    @PreAuthorize("hasRole('READER')")
    @Operation(summary = "获取我的通知列表")
    public Result<List<Notification>> getMyNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer readerId = resolveReaderIdFromAuth(auth.getName());
        List<Notification> notifications = notificationService.getNotificationsByReaderId(readerId);
        return Result.success(notifications);
    }

    /**
     * 获取读者的通知列表（管理员/馆员用）
     */
    @GetMapping("/reader/{readerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "获取读者的通知列表", description = "管理员/馆员查看指定读者的通知")
    public Result<List<Notification>> getNotificationsByReaderId(
            @Parameter(description = "读者ID") @PathVariable Integer readerId) {
        List<Notification> notifications = notificationService.getNotificationsByReaderId(readerId);
        return Result.success(notifications);
    }

    /**
     * 获取通知详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @Operation(summary = "获取通知详情")
    public Result<Notification> getNotificationById(
            @Parameter(description = "通知ID") @PathVariable Integer id) {
        Notification notification = notificationService.getNotificationById(id);
        return Result.success(notification);
    }

    /**
     * 获取当前读者的未读通知数量（readerId 从 JWT 获取）
     */
    @GetMapping("/unread/mine")
    @PreAuthorize("hasRole('READER')")
    @Operation(summary = "获取我的未读通知数量")
    public Result<Integer> countMyUnread() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer readerId = resolveReaderIdFromAuth(auth.getName());
        int count = notificationService.countUnread(readerId);
        return Result.success(count);
    }

    /**
     * 获取未读通知数量（管理员/馆员用）
     */
    @GetMapping("/unread/{readerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "获取读者的未读通知数量")
    public Result<Integer> countUnread(
            @Parameter(description = "读者ID") @PathVariable Integer readerId) {
        int count = notificationService.countUnread(readerId);
        return Result.success(count);
    }

    /**
     * 标记单条通知为已读（reader 需验证通知归属）
     */
    @PutMapping("/{id}/read")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @Operation(summary = "标记通知为已读")
    public Result<Void> markAsRead(
            @Parameter(description = "通知ID") @PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isReader = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_READER"));
        if (isReader) {
            // reader 只能标记自己的通知
            Integer readerId = resolveReaderIdFromAuth(auth.getName());
            Notification notification = notificationService.getNotificationById(id);
            if (!notification.getReaderId().equals(readerId)) {
                return Result.error("无权操作此通知");
            }
        }
        notificationService.markAsRead(id);
        return Result.success();
    }

    /**
     * 标记当前读者所有通知为已读（readerId 从 JWT 获取）
     */
    @PutMapping("/read-all/mine")
    @PreAuthorize("hasRole('READER')")
    @Operation(summary = "标记我的所有通知为已读")
    public Result<Void> markAllMyAsRead() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer readerId = resolveReaderIdFromAuth(auth.getName());
        notificationService.markAllAsRead(readerId);
        return Result.success();
    }

    /**
     * 标记所有通知为已读（管理员/馆员用）
     */
    @PutMapping("/read-all/{readerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "标记读者的所有通知为已读")
    public Result<Void> markAllAsRead(
            @Parameter(description = "读者ID") @PathVariable Integer readerId) {
        notificationService.markAllAsRead(readerId);
        return Result.success();
    }

    /**
     * 从 JWT principal（readerId 字符串）解析为数据库 id
     */
    private Integer resolveReaderIdFromAuth(String readerIdStr) {
        Reader reader = readerService.getReaderByReaderId(readerIdStr);
        return reader != null ? reader.getId() : null;
    }
}

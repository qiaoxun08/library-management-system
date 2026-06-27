package com.library.controller;

import com.library.annotation.OperLog;
import com.library.dto.Result;
import com.library.dto.ReviewReplyRequest;
import com.library.entity.Reader;
import com.library.entity.ReviewReply;
import com.library.service.ReaderService;
import com.library.service.ReviewReplyService;
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
 * 书评回复接口
 */
@RestController
@Tag(name = "书评回复", description = "书评回复的增删查相关接口")
public class ReviewReplyController {

    @Autowired
    private ReviewReplyService reviewReplyService;

    @Autowired
    private ReaderService readerService;

    /**
     * 发表回复（readerId 从 JWT 获取）
     */
    @PostMapping("/reviews/{reviewId}/replies")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @OperLog(module = "评论管理", action = "发表回复")
    @Operation(summary = "发表书评回复", description = "为指定书评添加回复")
    public Result<ReviewReply> addReply(
            @Parameter(description = "书评ID") @PathVariable Integer reviewId,
            @Valid @RequestBody ReviewReplyRequest request) {
        try {
            Integer readerId = resolveReaderIdFromAuth();
            ReviewReply reply = reviewReplyService.addReply(reviewId, readerId,
                    request.getContent(), request.getReplyToReaderId());
            return Result.success(reply);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除回复（readerId 从 JWT 获取，读者只能删自己的）
     */
    @DeleteMapping("/replies/{replyId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @OperLog(module = "评论管理", action = "删除回复")
    @Operation(summary = "删除书评回复", description = "删除指定回复，读者只能删除自己的")
    public Result<Void> deleteReply(
            @Parameter(description = "回复ID") @PathVariable Integer replyId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdminOrLibrarian = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_LIBRARIAN"));
            Integer readerId = isAdminOrLibrarian ? null : resolveReaderIdFromAuth();
            reviewReplyService.deleteReply(replyId, readerId, isAdminOrLibrarian);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取书评回复列表（分页）
     */
    @GetMapping("/reviews/{reviewId}/replies")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @Operation(summary = "获取书评回复列表", description = "分页获取指定书评的回复列表")
    public Result<Map<String, Object>> getReplies(
            @Parameter(description = "书评ID") @PathVariable Integer reviewId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int size) {
        try {
            Map<String, Object> replies = reviewReplyService.getReplies(reviewId, page, size);
            return Result.success(replies);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 从 JWT principal 解析 readerId
     */
    private Integer resolveReaderIdFromAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Reader reader = readerService.getReaderByReaderId(auth.getName());
        return reader != null ? reader.getId() : null;
    }
}

package com.library.controller;

import com.library.annotation.OperLog;
import com.library.dto.BookReviewAddRequest;
import com.library.dto.Result;
import com.library.entity.Reader;
import com.library.service.BookReviewService;
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
 * 图书评论接口
 */
@RestController
@RequestMapping("/reviews")
@Tag(name = "书评管理", description = "图书评论的增删改查、点赞相关接口")
public class BookReviewController {

    @Autowired
    private BookReviewService bookReviewService;

    /**
     * 添加评论（readerId 从 JWT 获取，不信前端传入）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @OperLog(module = "评论管理", action = "添加评论")
    @Operation(summary = "添加书评", description = "为图书添加评论，READER角色自动使用当前用户身份")
    public Result<Map<String, Object>> addReview(@Valid @RequestBody BookReviewAddRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isReader = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_READER"));
            // READER 角色：从 JWT 取 readerId，忽略前端传入
            // ADMIN/LIBRARIAN：可指定 readerId（管理场景）
            Integer readerId;
            if (isReader) {
                readerId = resolveReaderIdFromAuth(auth.getName());
            } else {
                readerId = request.getReaderId();
            }
            Map<String, Object> review = bookReviewService.addReview(request.getBookId(), readerId, request.getContent(), request.getRating());
            return Result.success(review);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除评论（readerId 从 JWT 获取，读者只能删自己的，管理员/馆员可删任意）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @OperLog(module = "评论管理", action = "删除评论")
    @Operation(summary = "删除书评", description = "删除指定评论，读者只能删除自己的")
    public Result<Void> deleteReview(
            @Parameter(description = "评论ID") @PathVariable Integer id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdminOrLibrarian = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_LIBRARIAN"));
            // readerId 从 JWT 获取，不信前端
            Integer readerId = isAdminOrLibrarian ? null : resolveReaderIdFromAuth(auth.getName());
            bookReviewService.deleteReview(id, readerId, isAdminOrLibrarian);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取图书评论列表（分页）
     */
    @GetMapping("/book/{bookId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @Operation(summary = "获取图书评论列表", description = "分页获取指定图书的评论")
    public Result<Map<String, Object>> getBookReviews(
            @Parameter(description = "图书ID") @PathVariable Integer bookId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        try {
            Map<String, Object> reviews = bookReviewService.getBookReviews(bookId, page, size);
            return Result.success(reviews);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前读者的评论列表（readerId 从 JWT 获取）
     */
    @GetMapping("/mine")
    @PreAuthorize("hasRole('READER')")
    @Operation(summary = "获取我的评论列表")
    public Result<Map<String, Object>> getMyReviews() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Integer readerId = resolveReaderIdFromAuth(auth.getName());
            Map<String, Object> reviews = bookReviewService.getReaderReviews(readerId);
            return Result.success(reviews);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取读者的评论列表（管理员/馆员用）
     */
    @GetMapping("/reader/{readerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "获取读者的评论列表", description = "管理员/馆员查看指定读者的所有评论")
    public Result<Map<String, Object>> getReaderReviews(
            @Parameter(description = "读者ID") @PathVariable Integer readerId) {
        try {
            Map<String, Object> reviews = bookReviewService.getReaderReviews(readerId);
            return Result.success(reviews);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 点赞评论（readerId 从 JWT 获取）
     */
    @PostMapping("/{id}/like")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @OperLog(module = "评论管理", action = "点赞评论")
    @Operation(summary = "点赞书评")
    public Result<Void> likeReview(
            @Parameter(description = "评论ID") @PathVariable Integer id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isReader = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_READER"));
            Integer readerId = isReader ? resolveReaderIdFromAuth(auth.getName()) : null;
            bookReviewService.likeReview(id, readerId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消点赞（readerId 从 JWT 获取）
     */
    @DeleteMapping("/{id}/like")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @OperLog(module = "评论管理", action = "取消点赞")
    @Operation(summary = "取消点赞书评")
    public Result<Void> unlikeReview(
            @Parameter(description = "评论ID") @PathVariable Integer id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isReader = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_READER"));
            Integer readerId = isReader ? resolveReaderIdFromAuth(auth.getName()) : null;
            bookReviewService.unlikeReview(id, readerId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查读者是否已点赞（readerId 从 JWT 获取）
     */
    @GetMapping("/{id}/liked")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @Operation(summary = "检查是否已点赞")
    public Result<Boolean> hasLiked(
            @Parameter(description = "评论ID") @PathVariable Integer id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isReader = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_READER"));
            Integer readerId = isReader ? resolveReaderIdFromAuth(auth.getName()) : null;
            boolean liked = bookReviewService.hasLiked(id, readerId);
            return Result.success(liked);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 从 JWT principal（readerId 字符串）解析为数据库 id
     */
    @Autowired
    private com.library.service.ReaderService readerService;

    private Integer resolveReaderIdFromAuth(String readerIdStr) {
        Reader reader = readerService.getReaderByReaderId(readerIdStr);
        return reader != null ? reader.getId() : null;
    }
}

package com.library.controller;

import com.library.dto.Result;
import com.library.dto.BorrowingDTO;
import com.library.entity.Borrowing;
import com.library.service.BorrowingService;
import com.library.annotation.OperLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/borrowings")
@Tag(name = "借阅管理", description = "图书借阅、归还、续借相关接口")
public class BorrowingController {

    @Autowired
    private BorrowingService borrowingService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "获取所有借阅记录")
    public Result<List<BorrowingDTO>> getAllBorrowings() {
        try {
            List<BorrowingDTO> borrowings = borrowingService.getAllBorrowings();
            return Result.success(borrowings);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @Operation(summary = "根据ID获取借阅详情")
    public Result<Borrowing> getBorrowingById(
            @Parameter(description = "借阅记录ID") @PathVariable Integer id) {
        try {
            Borrowing borrowing = borrowingService.getBorrowingById(id);
            return Result.success(borrowing);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/reader/{readerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or authentication.name == #readerId")
    @Operation(summary = "获取读者的借阅记录")
    public Result<List<Borrowing>> getBorrowingsByReaderId(
            @Parameter(description = "读者编号") @PathVariable String readerId) {
        try {
            List<Borrowing> borrowings = borrowingService.getBorrowingsByReaderIdString(readerId);
            return Result.success(borrowings);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/borrow")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @OperLog(module = "借阅管理", action = "借书")
    @Operation(summary = "借书", description = "读者借阅图书，READER角色自动使用当前用户身份")
    public Result<Borrowing> borrowBook(
            @Parameter(description = "读者编号") @RequestParam String readerId,
            @Parameter(description = "图书ID") @RequestParam Integer bookId) {
        try {
            // 如果当前用户是READER角色，使用当前用户的身份，忽略传入的readerId参数
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isReader = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_READER"));
            if (isReader) {
                readerId = auth.getName(); // principal 是 username (readerId)
            }
            Borrowing borrowing = borrowingService.borrowBook(readerId, bookId);
            return Result.success(borrowing);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/return/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @OperLog(module = "借阅管理", action = "还书")
    @Operation(summary = "还书", description = "归还已借阅的图书")
    public Result<Borrowing> returnBook(
            @Parameter(description = "借阅记录ID") @PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isReader = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_READER"));
        if (isReader) {
            borrowingService.validateOwnership(id, auth.getName());
        }
        Borrowing borrowing = borrowingService.returnBook(id);
        return Result.success(borrowing);
    }

    @PostMapping("/renew/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @OperLog(module = "借阅管理", action = "续借")
    @Operation(summary = "续借图书", description = "对已借阅的图书进行续借")
    public Result<Borrowing> renewBook(
            @Parameter(description = "借阅记录ID") @PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isReader = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_READER"));
        if (isReader) {
            borrowingService.validateOwnership(id, auth.getName());
        }
        Borrowing borrowing = borrowingService.renewBook(id);
        return Result.success(borrowing);
    }

    @PutMapping("/{id}/payFine")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @OperLog(module = "借阅管理", action = "缴纳罚款")
    @Operation(summary = "缴纳罚款", description = "为逾期借阅记录缴纳罚款")
    public Result<Void> payFine(
            @Parameter(description = "借阅记录ID") @PathVariable Integer id) {
        try {
            borrowingService.payFine(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

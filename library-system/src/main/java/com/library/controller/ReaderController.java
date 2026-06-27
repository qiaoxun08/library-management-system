package com.library.controller;

import com.library.dto.PasswordChangeRequest;
import com.library.dto.PasswordResetRequest;
import com.library.dto.Result;
import com.library.entity.Reader;
import com.library.service.ReaderService;
import com.library.annotation.OperLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/readers")
@Tag(name = "读者管理", description = "读者信息查询、修改、密码管理相关接口")
public class ReaderController {

    @Autowired
    private ReaderService readerService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "获取所有读者列表")
    public Result<List<Reader>> getAllReaders() {
        try {
            List<Reader> readers = readerService.getAllReaders();
            return Result.success(readers);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "根据ID获取读者详情")
    public Result<Reader> getReaderById(
            @Parameter(description = "读者ID") @PathVariable Integer id) {
        try {
            Reader reader = readerService.getReaderById(id);
            return Result.success(reader);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/readerId/{readerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or authentication.name == #readerId")
    @Operation(summary = "根据读者编号获取读者", description = "通过读者编号（readerId）查询读者信息")
    public Result<Reader> getReaderByReaderId(
            @Parameter(description = "读者编号") @PathVariable String readerId) {
        try {
            Reader reader = readerService.getReaderByReaderId(readerId);
            return Result.success(reader);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @OperLog(module = "读者管理", action = "添加读者")
    @Operation(summary = "新增读者", description = "管理员添加新读者账号")
    public Result<Reader> addReader(@RequestBody Reader reader) {
        try {
            Reader newReader = readerService.addReader(reader);
            return Result.success(newReader);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperLog(module = "读者管理", action = "修改读者")
    @Operation(summary = "修改读者信息", description = "管理员修改读者信息")
    public Result<Reader> updateReader(
            @Parameter(description = "读者ID") @PathVariable Integer id,
            @RequestBody Reader reader) {
        try {
            reader.setId(id);
            Reader updatedReader = readerService.updateReader(reader);
            return Result.success(updatedReader);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/profile/{readerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or authentication.name == #readerId")
    @Operation(summary = "更新个人资料", description = "读者更新自己的个人资料")
    public Result<Reader> updateReaderProfile(
            @Parameter(description = "读者编号") @PathVariable String readerId,
            @RequestBody Reader reader) {
        Reader updatedReader = readerService.updateProfile(readerId, reader);
        return Result.success(updatedReader);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperLog(module = "读者管理", action = "删除读者")
    @Operation(summary = "删除读者", description = "管理员删除读者账号")
    public Result<Void> deleteReader(
            @Parameter(description = "读者ID") @PathVariable Integer id) {
        try {
            readerService.deleteReader(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/payFine")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "读者缴纳罚款")
    public Result<Void> payFine(
            @Parameter(description = "读者ID") @PathVariable Integer id) {
        try {
            readerService.payFine(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/readerId/{readerId}/password")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or authentication.name == #readerId")
    @Operation(summary = "修改密码", description = "读者修改自己的密码")
    public Result<Void> changePassword(
            @Parameter(description = "读者编号") @PathVariable String readerId,
            @Valid @RequestBody PasswordChangeRequest request) {
        try {
            readerService.changePassword(readerId, request.getOldPassword(), request.getNewPassword());
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/readerId/{readerId}/resetPassword")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "重置密码", description = "管理员重置读者密码")
    public Result<Void> resetPassword(
            @Parameter(description = "读者编号") @PathVariable String readerId,
            @Valid @RequestBody PasswordResetRequest request) {
        try {
            readerService.resetPassword(readerId, request.getNewPassword());
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

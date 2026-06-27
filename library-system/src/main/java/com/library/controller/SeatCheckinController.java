package com.library.controller;

import com.library.annotation.OperLog;
import com.library.dto.Result;
import com.library.dto.SeatCheckinRequest;
import com.library.dto.SeatCheckoutRequest;
import com.library.entity.Reader;
import com.library.entity.SeatCheckin;
import com.library.service.ReaderService;
import com.library.service.SeatCheckinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 座位签到接口
 */
@RestController
@RequestMapping("/seats")
@Tag(name = "座位签到", description = "座位签到、签退相关接口")
public class SeatCheckinController {

    @Autowired
    private SeatCheckinService seatCheckinService;

    @Autowired
    private ReaderService readerService;

    /**
     * 获取所有签到记录
     */
    @GetMapping("/checkins")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "获取所有签到记录")
    public Result<List<SeatCheckin>> getAllCheckins() {
        List<SeatCheckin> checkins = seatCheckinService.getAllCheckins();
        return Result.success(checkins);
    }

    /**
     * 获取当前读者的签到记录（readerId 从 JWT 获取）
     */
    @GetMapping("/checkins/mine")
    @PreAuthorize("hasRole('READER')")
    @Operation(summary = "获取我的签到记录", description = "获取当前登录读者的签到历史")
    public Result<List<SeatCheckin>> getMyCheckins() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer readerId = resolveReaderIdFromAuth(auth.getName());
        List<SeatCheckin> checkins = seatCheckinService.getCheckinsByReaderId(readerId);
        return Result.success(checkins);
    }

    /**
     * 获取读者的签到记录（管理员/馆员用）
     */
    @GetMapping("/checkins/reader/{readerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "获取读者的签到记录", description = "管理员/馆员查看指定读者的签到历史")
    public Result<List<SeatCheckin>> getCheckinsByReaderId(
            @Parameter(description = "读者ID") @PathVariable Integer readerId) {
        List<SeatCheckin> checkins = seatCheckinService.getCheckinsByReaderId(readerId);
        return Result.success(checkins);
    }

    /**
     * 座位签到（readerId 从 JWT 获取，不信前端传入）
     */
    @PostMapping("/checkin")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @OperLog(module = "座位签到", action = "签到")
    @Operation(summary = "座位签到", description = "对已预约的座位进行签到")
    public Result<SeatCheckin> checkin(@Valid @RequestBody SeatCheckinRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isReader = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_READER"));
        Integer readerId = isReader ? resolveReaderIdFromAuth(auth.getName()) : request.getReaderId();
        SeatCheckin checkin = seatCheckinService.checkin(request.getSeatId(), readerId, request.getReservationId());
        return Result.success(checkin);
    }

    /**
     * 座位签退（reader 需验证签到记录归属）
     */
    @PostMapping("/checkout")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @OperLog(module = "座位签到", action = "签退")
    @Operation(summary = "座位签退", description = "结束座位使用，签退离座")
    public Result<SeatCheckin> checkout(@Valid @RequestBody SeatCheckoutRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isReader = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_READER"));
        if (isReader) {
            // reader 只能签退自己的座位
            Integer readerId = resolveReaderIdFromAuth(auth.getName());
            SeatCheckin checkin = seatCheckinService.getCheckinById(request.getCheckinId());
            if (!checkin.getReaderId().equals(readerId)) {
                return Result.error("无权签退他人座位");
            }
        }
        SeatCheckin checkin = seatCheckinService.checkout(request.getCheckinId());
        return Result.success(checkin);
    }

    /**
     * 从 JWT principal（readerId 字符串）解析为数据库 id
     */
    private Integer resolveReaderIdFromAuth(String readerIdStr) {
        Reader reader = readerService.getReaderByReaderId(readerIdStr);
        return reader != null ? reader.getId() : null;
    }
}

package com.library.controller;

import com.library.dto.Result;
import com.library.entity.Seat;
import com.library.service.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/seats")
@Tag(name = "座位管理", description = "座位查询、添加、修改相关接口")
public class SeatController {

    @Autowired
    private SeatService seatService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @Operation(summary = "获取所有座位列表")
    public Result<List<Seat>> getAllSeats() {
        try {
            List<Seat> seats = seatService.getAllSeats();
            return Result.success(seats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @Operation(summary = "根据ID获取座位详情")
    public Result<Seat> getSeatById(
            @Parameter(description = "座位ID") @PathVariable Integer id) {
        try {
            Seat seat = seatService.getSeatById(id);
            return Result.success(seat);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/seatNumber/{seatNumber}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @Operation(summary = "根据座位号获取座位", description = "通过座位编号查询座位信息")
    public Result<Seat> getSeatBySeatNumber(
            @Parameter(description = "座位编号") @PathVariable String seatNumber) {
        try {
            Seat seat = seatService.getSeatBySeatNumber(seatNumber);
            return Result.success(seat);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/area/{area}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @Operation(summary = "按区域获取座位列表")
    public Result<List<Seat>> getSeatsByArea(
            @Parameter(description = "区域名称") @PathVariable String area) {
        try {
            List<Seat> seats = seatService.getSeatsByArea(area);
            return Result.success(seats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @Operation(summary = "按状态获取座位列表")
    public Result<List<Seat>> getSeatsByStatus(
            @Parameter(description = "座位状态") @PathVariable Integer status) {
        try {
            List<Seat> seats = seatService.getSeatsByStatus(status);
            return Result.success(seats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "新增座位")
    public Result<Seat> addSeat(@RequestBody Seat seat) {
        try {
            Seat newSeat = seatService.addSeat(seat);
            return Result.success(newSeat);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "更新座位信息")
    public Result<Seat> updateSeat(
            @Parameter(description = "座位ID") @PathVariable Integer id,
            @RequestBody Seat seat) {
        try {
            seat.setId(id);
            Seat updatedSeat = seatService.updateSeat(seat);
            return Result.success(updatedSeat);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除座位")
    public Result<Void> deleteSeat(
            @Parameter(description = "座位ID") @PathVariable Integer id) {
        try {
            seatService.deleteSeat(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "更新座位状态")
    public Result<Void> updateSeatStatus(
            @Parameter(description = "座位ID") @PathVariable Integer id,
            @Parameter(description = "目标状态") @PathVariable Integer status) {
        try {
            seatService.updateSeatStatus(id, status);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/timeline")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @Operation(summary = "获取座位时间轴视图", description = "返回每个座位在各时段（8:00-22:00）的占用状态")
    public Result<List<Map<String, Object>>> getSeatTimeline(
            @Parameter(description = "日期，格式 yyyy-MM-dd") @RequestParam String date) {
        try {
            List<Map<String, Object>> timeline = seatService.getSeatTimeline(date);
            return Result.success(timeline);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

package com.library.controller;

import com.library.dto.Result;
import com.library.entity.Admin;
import com.library.entity.Librarian;
import com.library.entity.Reader;
import com.library.entity.Seat;
import com.library.service.AdminService;
import com.library.service.ReaderService;
import com.library.service.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Tag(name = "管理员管理", description = "管理员/馆员账号管理、读者与座位管理相关接口")
public class AdminController {

    @Autowired
    private ReaderService readerService;

    @Autowired
    private SeatService seatService;

    @Autowired
    private AdminService adminService;

    // Reader management endpoints
    @GetMapping("/readers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取所有读者列表")
    public Result<List<Reader>> getAllReaders() {
        try {
            List<Reader> readers = readerService.getAllReaders();
            return Result.success(readers);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/readers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
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

    @PostMapping("/readers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "新增读者", description = "管理员添加新读者")
    public Result<Reader> addReader(@RequestBody Reader reader) {
        try {
            Reader newReader = readerService.addReader(reader);
            return Result.success(newReader);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/readers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "修改读者信息")
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

    @DeleteMapping("/readers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除读者")
    public Result<Void> deleteReader(
            @Parameter(description = "读者ID") @PathVariable Integer id) {
        try {
            readerService.deleteReader(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // Seat management endpoints
    @GetMapping("/seats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取所有座位列表")
    public Result<List<Seat>> getAllSeats() {
        try {
            List<Seat> seats = seatService.getAllSeats();
            return Result.success(seats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/seats/{id}")
    @PreAuthorize("hasRole('ADMIN')")
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

    @PostMapping("/seats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "新增座位")
    public Result<Seat> addSeat(@RequestBody Seat seat) {
        try {
            Seat newSeat = seatService.addSeat(seat);
            return Result.success(newSeat);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/seats/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "修改座位信息")
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

    @DeleteMapping("/seats/{id}")
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

    @PutMapping("/seats/{id}/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
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

    // ==================== Account Management ====================

    // Admin account management
    @GetMapping("/accounts/admins")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取所有管理员账号")
    public Result<List<Admin>> getAllAdmins() {
        try {
            return Result.success(adminService.getAllAdmins());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/accounts/admins")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "新增管理员账号")
    public Result<Admin> addAdmin(@RequestBody Admin admin) {
        try {
            return Result.success(adminService.addAdmin(admin));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/accounts/admins/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "修改管理员账号")
    public Result<Admin> updateAdmin(
            @Parameter(description = "管理员ID") @PathVariable Integer id,
            @RequestBody Admin admin) {
        try {
            admin.setId(id);
            return Result.success(adminService.updateAdmin(admin));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/accounts/admins/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除管理员账号")
    public Result<Void> deleteAdmin(
            @Parameter(description = "管理员ID") @PathVariable Integer id) {
        try {
            adminService.deleteAdmin(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // Librarian account management
    @GetMapping("/accounts/librarians")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取所有馆员账号")
    public Result<List<Librarian>> getAllLibrarians() {
        try {
            return Result.success(adminService.getAllLibrarians());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/accounts/librarians")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "新增馆员账号")
    public Result<Librarian> addLibrarian(@RequestBody Librarian librarian) {
        try {
            return Result.success(adminService.addLibrarian(librarian));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/accounts/librarians/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "修改馆员账号")
    public Result<Librarian> updateLibrarian(
            @Parameter(description = "馆员ID") @PathVariable Integer id,
            @RequestBody Librarian librarian) {
        try {
            librarian.setId(id);
            return Result.success(adminService.updateLibrarian(librarian));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/accounts/librarians/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除馆员账号")
    public Result<Void> deleteLibrarian(
            @Parameter(description = "馆员ID") @PathVariable Integer id) {
        try {
            adminService.deleteLibrarian(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

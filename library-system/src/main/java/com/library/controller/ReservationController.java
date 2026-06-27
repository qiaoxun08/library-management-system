package com.library.controller;

import com.library.dto.Result;
import com.library.dto.ReservationDTO;
import com.library.entity.Reader;
import com.library.entity.Reservation;
import com.library.mapper.ReaderMapper;
import com.library.service.ReservationService;
import com.library.annotation.OperLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@Tag(name = "预约管理", description = "座位预约、取消预约相关接口")
public class ReservationController {

    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReaderMapper readerMapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "获取所有预约记录")
    public Result<List<ReservationDTO>> getAllReservations() {
        try {
            List<ReservationDTO> reservations = reservationService.getAllReservations();
            return Result.success(reservations);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @Operation(summary = "根据ID获取预约详情")
    public Result<ReservationDTO> getReservationById(
            @Parameter(description = "预约记录ID") @PathVariable Integer id) {
        try {
            ReservationDTO reservation = reservationService.getReservationById(id);
            return Result.success(reservation);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/reader/{readerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or authentication.name == #readerId")
    @Operation(summary = "获取读者的预约记录")
    public Result<List<ReservationDTO>> getReservationsByReaderId(
            @Parameter(description = "读者编号") @PathVariable String readerId) {
        try {
            List<ReservationDTO> reservations = reservationService.getReservationsByReaderIdString(readerId);
            return Result.success(reservations);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/book/{bookId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @Operation(summary = "获取图书的预约记录")
    public Result<List<ReservationDTO>> getReservationsByBookId(
            @Parameter(description = "图书ID") @PathVariable Integer bookId) {
        try {
            List<ReservationDTO> reservations = reservationService.getReservationsByBookId(bookId);
            return Result.success(reservations);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "按状态筛选预约记录")
    public Result<List<ReservationDTO>> getReservationsByStatus(
            @Parameter(description = "预约状态") @PathVariable Integer status) {
        try {
            List<ReservationDTO> reservations = reservationService.getReservationsByStatus(status);
            return Result.success(reservations);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @OperLog(module = "预约管理", action = "创建预约")
    @Operation(summary = "创建预约", description = "创建座位预约，READER角色自动使用当前用户身份")
    public Result<Reservation> addReservation(@RequestBody Reservation reservation) {
        try {
            // READER 角色：readerId 从 JWT 获取，忽略前端传入
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isReader = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_READER"));
            if (isReader) {
                Reader currentUser = readerMapper.findByReaderId(auth.getName());
                if (currentUser != null) {
                    reservation.setReaderId(currentUser.getId());
                }
            }
            Reservation newReservation = reservationService.addReservation(reservation);
            return Result.success(newReservation);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "更新预约信息")
    public Result<Reservation> updateReservation(
            @Parameter(description = "预约记录ID") @PathVariable Integer id,
            @RequestBody Reservation reservation) {
        try {
            reservation.setId(id);
            Reservation updatedReservation = reservationService.updateReservation(reservation);
            return Result.success(updatedReservation);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @OperLog(module = "预约管理", action = "取消预约")
    @Operation(summary = "取消预约", description = "取消已有的预约，读者只能取消自己的预约")
    public Result<Void> deleteReservation(
            @Parameter(description = "预约记录ID") @PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("deleteReservation: id={}, user={}, roles={}", id, auth.getName(), auth.getAuthorities());
        try {
            // 如果当前用户是READER角色，验证是否是自己的预约
            boolean isReader = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_READER"));
            if (isReader) {
                ReservationDTO reservation = reservationService.getReservationById(id);
                if (reservation == null) {
                    return Result.error(404, "预约记录不存在");
                }
                // 获取当前用户的readerId
                Reader currentUser = readerMapper.findByReaderId(auth.getName());
                if (currentUser == null) {
                    return Result.error(401, "用户不存在");
                }
                if (!currentUser.getId().equals(reservation.getReaderId())) {
                    return Result.error(403, "只能删除自己的预约");
                }
            }
            reservationService.deleteReservation(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @OperLog(module = "预约管理", action = "审批预约")
    @Operation(summary = "审批预约", description = "更新预约状态（审批通过/拒绝）")
    public Result<Void> updateReservationStatus(
            @Parameter(description = "预约记录ID") @PathVariable Integer id,
            @Parameter(description = "预约状态") @PathVariable Integer status) {
        reservationService.updateReservationStatus(id, status);
        return Result.success();
    }
}

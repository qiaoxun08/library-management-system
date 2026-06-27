package com.library.controller;

import com.library.dto.Result;
import com.library.entity.Reader;
import com.library.service.ReaderService;
import com.library.service.SeatRecommendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 智能座位推荐接口
 */
@RestController
@RequestMapping("/seats/recommend")
@Tag(name = "座位推荐", description = "智能座位推荐相关接口")
public class SeatRecommendController {

    @Autowired
    private SeatRecommendService seatRecommendService;

    @Autowired
    private ReaderService readerService;

    /**
     * 智能推荐座位（readerId 从 JWT 获取，不信前端传入）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('READER')")
    @Operation(summary = "智能推荐座位", description = "根据日期和时段智能推荐可用座位")
    public Result<List<Map<String, Object>>> recommendSeats(
            @Parameter(description = "预约日期，如2026-06-27") @RequestParam String date,
            @Parameter(description = "时间段，如09:00-12:00") @RequestParam String timeSlot,
            @Parameter(description = "推荐数量") @RequestParam(defaultValue = "3") int limit) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isReader = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_READER"));
            Integer readerId;
            if (isReader) {
                readerId = resolveReaderIdFromAuth(auth.getName());
            } else {
                // ADMIN/LIBRARIAN：可指定 readerId（通过额外参数）
                readerId = null; // 后续可加 @RequestParam(required = false)
            }
            List<Map<String, Object>> recommendations = seatRecommendService.recommendSeats(readerId, date, timeSlot, limit);
            return Result.success(recommendations);
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

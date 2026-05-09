package com.smartledger.controller;

import com.smartledger.common.Result;
import com.smartledger.dto.response.CategoryStatResponse;
import com.smartledger.dto.response.OverviewResponse;
import com.smartledger.dto.response.TrendResponse;
import com.smartledger.exception.BusinessException;
import com.smartledger.security.UserContext;
import com.smartledger.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@Tag(name = "统计报表")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/overview")
    @Operation(summary = "获取概览数据")
    public Result<OverviewResponse> getOverview(@RequestParam Long bookId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return Result.success(statisticsService.getOverview(userId, bookId));
    }

    @GetMapping("/trend")
    @Operation(summary = "获取趋势数据")
    public Result<TrendResponse> getTrend(
            @RequestParam Long bookId,
            @RequestParam(defaultValue = "week") String period,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return Result.success(statisticsService.getTrend(userId, bookId, period, startDate, endDate));
    }

    @GetMapping("/category")
    @Operation(summary = "获取分类统计")
    public Result<List<CategoryStatResponse>> getCategoryStats(
            @RequestParam Long bookId,
            @RequestParam(defaultValue = "2") Integer type,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return Result.success(statisticsService.getCategoryStats(userId, bookId, type, startDate, endDate));
    }
}

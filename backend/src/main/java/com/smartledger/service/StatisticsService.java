package com.smartledger.service;

import com.smartledger.dto.response.CategoryStatResponse;
import com.smartledger.dto.response.OverviewResponse;
import com.smartledger.dto.response.TrendResponse;
import java.time.LocalDate;
import java.util.List;

public interface StatisticsService {
    OverviewResponse getOverview(Long userId, Long bookId);
    TrendResponse getTrend(Long userId, Long bookId, String period, LocalDate startDate, LocalDate endDate);
    List<CategoryStatResponse> getCategoryStats(Long userId, Long bookId, Integer type, LocalDate startDate, LocalDate endDate);
}

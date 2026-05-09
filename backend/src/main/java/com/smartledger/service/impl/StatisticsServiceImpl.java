package com.smartledger.service.impl;

import com.smartledger.dto.response.CategoryStatResponse;
import com.smartledger.dto.response.OverviewResponse;
import com.smartledger.dto.response.TrendResponse;
import com.smartledger.mapper.TransactionMapper;
import com.smartledger.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private TransactionMapper transactionMapper;

    @Override
    public OverviewResponse getOverview(Long userId, Long bookId) {
        OverviewResponse response = new OverviewResponse();
        LocalDate today = LocalDate.now();

        response.setTodayIncome(getSum(userId, bookId, 1, today, today));
        response.setTodayExpense(getSum(userId, bookId, 2, today, today));

        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        response.setWeekIncome(getSum(userId, bookId, 1, weekStart, weekEnd));
        response.setWeekExpense(getSum(userId, bookId, 2, weekStart, weekEnd));

        LocalDate monthStart = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate monthEnd = today.with(TemporalAdjusters.lastDayOfMonth());
        response.setMonthIncome(getSum(userId, bookId, 1, monthStart, monthEnd));
        response.setMonthExpense(getSum(userId, bookId, 2, monthStart, monthEnd));

        LocalDate yearStart = today.with(TemporalAdjusters.firstDayOfYear());
        LocalDate yearEnd = today.with(TemporalAdjusters.lastDayOfYear());
        response.setYearIncome(getSum(userId, bookId, 1, yearStart, yearEnd));
        response.setYearExpense(getSum(userId, bookId, 2, yearStart, yearEnd));

        return response;
    }

    @Override
    public TrendResponse getTrend(Long userId, Long bookId, String period, LocalDate startDate, LocalDate endDate) {
        TrendResponse response = new TrendResponse();
        List<String> labels = new ArrayList<>();
        List<BigDecimal> income = new ArrayList<>();
        List<BigDecimal> expense = new ArrayList<>();

        if ("month".equals(period)) {
            if (startDate == null) {
                startDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
            }
            if (endDate == null) {
                endDate = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
            }

            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                labels.add(String.valueOf(current.getDayOfMonth()));
                income.add(getSum(userId, bookId, 1, current, current));
                expense.add(getSum(userId, bookId, 2, current, current));
                current = current.plusDays(1);
            }
        } else if ("year".equals(period)) {
            if (startDate == null) {
                startDate = LocalDate.now().with(TemporalAdjusters.firstDayOfYear());
            }
            if (endDate == null) {
                endDate = LocalDate.now().with(TemporalAdjusters.lastDayOfYear());
            }

            for (int month = 1; month <= 12; month++) {
                LocalDate monthStart = LocalDate.of(startDate.getYear(), month, 1);
                LocalDate monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth());
                labels.add(month + "月");
                income.add(getSum(userId, bookId, 1, monthStart, monthEnd));
                expense.add(getSum(userId, bookId, 2, monthStart, monthEnd));
            }
        } else {
            if (startDate == null) {
                startDate = LocalDate.now().minusDays(6);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }

            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                labels.add(current.getMonthValue() + "/" + current.getDayOfMonth());
                income.add(getSum(userId, bookId, 1, current, current));
                expense.add(getSum(userId, bookId, 2, current, current));
                current = current.plusDays(1);
            }
        }

        response.setLabels(labels);
        response.setIncome(income);
        response.setExpense(expense);
        return response;
    }

    @Override
    public List<CategoryStatResponse> getCategoryStats(Long userId, Long bookId, Integer type, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        }
        if (endDate == null) {
            endDate = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
        }

        List<Map<String, Object>> stats = transactionMapper.sumByCategory(userId, bookId, type, startDate, endDate);
        List<CategoryStatResponse> result = new ArrayList<>();

        BigDecimal total = stats.stream()
                .map(s -> (BigDecimal) s.get("amount"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (Map<String, Object> stat : stats) {
            CategoryStatResponse resp = new CategoryStatResponse();
            resp.setCategoryId(((Number) stat.get("categoryId")).longValue());
            resp.setCategoryName((String) stat.get("categoryName"));
            resp.setCategoryIcon((String) stat.get("categoryIcon"));
            BigDecimal amount = (BigDecimal) stat.get("amount");
            resp.setAmount(amount);

            if (total.compareTo(BigDecimal.ZERO) > 0) {
                resp.setPercentage(amount.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP));
            } else {
                resp.setPercentage(BigDecimal.ZERO);
            }
            result.add(resp);
        }

        return result;
    }

    private BigDecimal getSum(Long userId, Long bookId, Integer type, LocalDate startDate, LocalDate endDate) {
        BigDecimal sum = transactionMapper.sumAmountByDateRange(userId, bookId, type, startDate, endDate);
        return sum != null ? sum : BigDecimal.ZERO;
    }
}

package com.smartledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartledger.dto.request.BudgetRequest;
import com.smartledger.dto.response.BudgetResponse;
import com.smartledger.entity.Budget;
import com.smartledger.entity.Category;
import com.smartledger.exception.BusinessException;
import com.smartledger.mapper.BudgetMapper;
import com.smartledger.mapper.CategoryMapper;
import com.smartledger.mapper.TransactionMapper;
import com.smartledger.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class BudgetServiceImpl implements BudgetService {

    @Autowired
    private BudgetMapper budgetMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    @Override
    public List<BudgetResponse> getBudgets(Long userId, Long bookId, String yearMonth) {
        if (yearMonth == null) {
            yearMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        LambdaQueryWrapper<Budget> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Budget::getUserId, userId)
               .eq(Budget::getBookId, bookId)
               .eq(Budget::getYearMonth, yearMonth);
        List<Budget> budgets = budgetMapper.selectList(wrapper);

        List<BudgetResponse> result = new ArrayList<>();
        for (Budget budget : budgets) {
            result.add(toResponse(budget, userId, bookId));
        }
        return result;
    }

    @Override
    public BudgetResponse createOrUpdateBudget(Long userId, BudgetRequest request) {
        LambdaQueryWrapper<Budget> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Budget::getUserId, userId)
               .eq(Budget::getBookId, request.getBookId())
               .eq(Budget::getCategoryId, request.getCategoryId())
               .eq(Budget::getYearMonth, request.getYearMonth());
        Budget budget = budgetMapper.selectOne(wrapper);

        if (budget == null) {
            budget = new Budget();
            budget.setUserId(userId);
            budget.setBookId(request.getBookId());
            budget.setCategoryId(request.getCategoryId());
            budget.setYearMonth(request.getYearMonth());
            budget.setAmount(request.getAmount());
            budget.setCreatedAt(LocalDateTime.now());
            budget.setUpdatedAt(LocalDateTime.now());
            budgetMapper.insert(budget);
        } else {
            budget.setAmount(request.getAmount());
            budget.setUpdatedAt(LocalDateTime.now());
            budgetMapper.updateById(budget);
        }

        return toResponse(budget, userId, request.getBookId());
    }

    @Override
    public void deleteBudget(Long userId, Long id) {
        Budget budget = budgetMapper.selectById(id);
        if (budget == null || !budget.getUserId().equals(userId)) {
            throw new BusinessException(404, "预算不存在");
        }
        budgetMapper.deleteById(id);
    }

    private BudgetResponse toResponse(Budget budget, Long userId, Long bookId) {
        BudgetResponse response = new BudgetResponse();
        response.setId(budget.getId());
        response.setBookId(budget.getBookId());
        response.setCategoryId(budget.getCategoryId());
        response.setYearMonth(budget.getYearMonth());
        response.setAmount(budget.getAmount());

        if (budget.getCategoryId() == 0) {
            response.setCategoryName("总预算");
        } else {
            Category category = categoryMapper.selectById(budget.getCategoryId());
            if (category != null) {
                response.setCategoryName(category.getName());
            }
        }

        YearMonth ym = YearMonth.parse(budget.getYearMonth());
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        BigDecimal used;
        if (budget.getCategoryId() == 0) {
            used = transactionMapper.sumAmountByDateRange(userId, bookId, 2, startDate, endDate);
        } else {
            LambdaQueryWrapper<com.smartledger.entity.Transaction> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(com.smartledger.entity.Transaction::getUserId, userId)
                   .eq(com.smartledger.entity.Transaction::getBookId, bookId)
                   .eq(com.smartledger.entity.Transaction::getCategoryId, budget.getCategoryId())
                   .eq(com.smartledger.entity.Transaction::getType, 2)
                   .ge(com.smartledger.entity.Transaction::getTransactionDate, startDate)
                   .le(com.smartledger.entity.Transaction::getTransactionDate, endDate);
            used = transactionMapper.sumAmountByDateRange(userId, bookId, 2, startDate, endDate);
        }

        response.setUsed(used != null ? used : BigDecimal.ZERO);

        if (budget.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            response.setPercentage(response.getUsed().multiply(BigDecimal.valueOf(100))
                    .divide(budget.getAmount(), 2, RoundingMode.HALF_UP));
        } else {
            response.setPercentage(BigDecimal.ZERO);
        }

        return response;
    }
}

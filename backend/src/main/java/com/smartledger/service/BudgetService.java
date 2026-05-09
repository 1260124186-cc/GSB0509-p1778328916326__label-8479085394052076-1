package com.smartledger.service;

import com.smartledger.dto.request.BudgetRequest;
import com.smartledger.dto.response.BudgetResponse;
import java.util.List;

public interface BudgetService {
    List<BudgetResponse> getBudgets(Long userId, Long bookId, String yearMonth);
    BudgetResponse createOrUpdateBudget(Long userId, BudgetRequest request);
    void deleteBudget(Long userId, Long id);
}

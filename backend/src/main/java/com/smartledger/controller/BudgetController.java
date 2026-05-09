package com.smartledger.controller;

import com.smartledger.common.Result;
import com.smartledger.dto.request.BudgetRequest;
import com.smartledger.dto.response.BudgetResponse;
import com.smartledger.exception.BusinessException;
import com.smartledger.security.UserContext;
import com.smartledger.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@Tag(name = "预算管理")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @GetMapping
    @Operation(summary = "获取预算列表")
    public Result<List<BudgetResponse>> getBudgets(
            @RequestParam Long bookId,
            @RequestParam(required = false) String yearMonth) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return Result.success(budgetService.getBudgets(userId, bookId, yearMonth));
    }

    @PostMapping
    @Operation(summary = "创建或更新预算")
    public Result<BudgetResponse> createOrUpdateBudget(@Valid @RequestBody BudgetRequest request) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return Result.success(budgetService.createOrUpdateBudget(userId, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除预算")
    public Result<Void> deleteBudget(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        budgetService.deleteBudget(userId, id);
        return Result.success();
    }
}

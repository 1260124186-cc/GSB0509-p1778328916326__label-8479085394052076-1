package com.smartledger.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartledger.common.PageResult;
import com.smartledger.common.Result;
import com.smartledger.dto.request.TransactionRequest;
import com.smartledger.dto.response.TransactionResponse;
import com.smartledger.exception.BusinessException;
import com.smartledger.security.UserContext;
import com.smartledger.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "记录管理")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping
    @Operation(summary = "获取记录列表")
    public Result<PageResult<TransactionResponse>> getTransactions(
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long tagId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        Page<TransactionResponse> result = transactionService.getTransactions(
                userId, bookId, type, categoryId, startDate, endDate, keyword, tagId, page, size);
        PageResult<TransactionResponse> pageResult = new PageResult<>(
                result.getRecords(), result.getTotal(), result.getPages(), result.getCurrent(), result.getSize());
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取记录详情")
    public Result<TransactionResponse> getTransaction(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return Result.success(transactionService.getTransaction(userId, id));
    }

    @PostMapping
    @Operation(summary = "创建记录")
    public Result<TransactionResponse> createTransaction(@Valid @RequestBody TransactionRequest request) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        // 验证交易日期不能是未来日期
        if (request.getTransactionDate() != null && request.getTransactionDate().isAfter(LocalDate.now())) {
            throw new BusinessException(400, "交易日期不能超过今天");
        }
        return Result.success(transactionService.createTransaction(userId, request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新记录")
    public Result<TransactionResponse> updateTransaction(@PathVariable Long id, @Valid @RequestBody TransactionRequest request) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        // 验证交易日期不能是未来日期
        if (request.getTransactionDate() != null && request.getTransactionDate().isAfter(LocalDate.now())) {
            throw new BusinessException(400, "交易日期不能超过今天");
        }
        return Result.success(transactionService.updateTransaction(userId, id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除记录")
    public Result<Void> deleteTransaction(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        transactionService.deleteTransaction(userId, id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除记录")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        transactionService.batchDelete(userId, ids);
        return Result.success();
    }

    @GetMapping("/export")
    @Operation(summary = "导出记录为CSV")
    public void exportTransactions(
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long tagId,
            HttpServletResponse response) throws IOException {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"records_" + LocalDate.now() + ".csv\"");

        java.io.PrintWriter writer = response.getWriter();
        writer.write("\uFEFF"); // UTF-8 BOM
        writer.write("日期,类型,分类,金额,备注,支付方式\n");

        List<TransactionResponse> transactions = transactionService.getTransactions(
                userId, bookId, type, categoryId, startDate, endDate, keyword, tagId, 1, 10000).getRecords();

        for (TransactionResponse t : transactions) {
            String typeStr = t.getType() == 1 ? "收入" : "支出";
            String date = t.getTransactionDate().toString();
            String category = t.getCategoryName() != null ? t.getCategoryName() : "";
            String amount = String.format("%.2f", t.getAmount());
            String remark = t.getRemark() != null ? t.getRemark().replace(",", "，") : "";
            String payment = t.getPaymentMethod() != null ? t.getPaymentMethod() : "";
            writer.write(String.format("%s,%s,%s,%s,%s,%s\n", date, typeStr, category, amount, remark, payment));
        }
        writer.flush();
    }
}

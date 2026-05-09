package com.smartledger.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartledger.dto.request.TransactionRequest;
import com.smartledger.dto.response.TransactionResponse;
import java.time.LocalDate;
import java.util.List;

public interface TransactionService {
    Page<TransactionResponse> getTransactions(Long userId, Long bookId, Integer type, Long categoryId,
                                              LocalDate startDate, LocalDate endDate, String keyword, Long tagId,
                                              int page, int size);
    TransactionResponse getTransaction(Long userId, Long id);
    TransactionResponse createTransaction(Long userId, TransactionRequest request);
    TransactionResponse updateTransaction(Long userId, Long id, TransactionRequest request);
    void deleteTransaction(Long userId, Long id);
    void batchDelete(Long userId, List<Long> ids);
}

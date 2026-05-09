package com.smartledger.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BudgetResponse {
    private Long id;
    private Long bookId;
    private Long categoryId;
    private String categoryName;
    private String yearMonth;
    private BigDecimal amount;
    private BigDecimal used;
    private BigDecimal percentage;
}

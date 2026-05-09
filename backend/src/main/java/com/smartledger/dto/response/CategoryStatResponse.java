package com.smartledger.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CategoryStatResponse {
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private BigDecimal amount;
    private BigDecimal percentage;
}

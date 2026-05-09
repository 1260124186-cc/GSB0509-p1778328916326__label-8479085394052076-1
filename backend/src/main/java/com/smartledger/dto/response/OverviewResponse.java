package com.smartledger.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OverviewResponse {
    private BigDecimal todayIncome = BigDecimal.ZERO;
    private BigDecimal todayExpense = BigDecimal.ZERO;
    private BigDecimal weekIncome = BigDecimal.ZERO;
    private BigDecimal weekExpense = BigDecimal.ZERO;
    private BigDecimal monthIncome = BigDecimal.ZERO;
    private BigDecimal monthExpense = BigDecimal.ZERO;
    private BigDecimal yearIncome = BigDecimal.ZERO;
    private BigDecimal yearExpense = BigDecimal.ZERO;
}

package com.smartledger.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class TrendResponse {
    private List<String> labels;
    private List<BigDecimal> income;
    private List<BigDecimal> expense;
}

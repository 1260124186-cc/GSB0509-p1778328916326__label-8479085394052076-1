package com.smartledger.dto.request;

import lombok.Data;
import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class BudgetRequest {
    @NotNull(message = "账本ID不能为空")
    private Long bookId;

    private Long categoryId = 0L;

    @NotBlank(message = "预算月份不能为空")
    @Pattern(regexp = "\\d{4}-\\d{2}", message = "月份格式应为yyyy-MM")
    private String yearMonth;

    @NotNull(message = "预算金额不能为空")
    @DecimalMin(value = "0.01", message = "预算金额必须大于0")
    private BigDecimal amount;
}

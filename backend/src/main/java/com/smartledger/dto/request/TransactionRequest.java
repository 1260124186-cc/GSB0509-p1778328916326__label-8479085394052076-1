package com.smartledger.dto.request;

import lombok.Data;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class TransactionRequest {
    @NotNull(message = "账本ID不能为空")
    private Long bookId;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @NotNull(message = "类型不能为空")
    private Integer type;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    @DecimalMax(value = "99999999.99", message = "金额不能超过99999999.99")
    private BigDecimal amount;

    @NotNull(message = "交易日期不能为空")
    private LocalDate transactionDate;

    @Size(max = 200, message = "备注不能超过200个字符")
    private String remark;

    private String paymentMethod;

    private List<Long> tagIds;

    private List<String> images;
}

package com.smartledger.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("transaction")
public class Transaction {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long bookId;

    private Long categoryId;

    /**
     * 1: 收入, 2: 支出
     */
    private Integer type;

    private BigDecimal amount;

    private LocalDate transactionDate;

    private String remark;

    private String paymentMethod;

    private String images;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

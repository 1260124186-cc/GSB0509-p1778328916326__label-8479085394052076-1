package com.smartledger.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("transaction_tag")
public class TransactionTag {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long transactionId;

    private Long tagId;
}

package com.smartledger.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("category")
public class Category {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    private String icon;

    /**
     * 1: 收入, 2: 支出
     */
    private Integer type;

    private Integer isSystem;

    private Integer sortOrder;

    private Integer isHidden;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

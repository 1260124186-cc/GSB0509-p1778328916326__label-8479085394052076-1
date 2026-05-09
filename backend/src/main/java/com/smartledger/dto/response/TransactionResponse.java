package com.smartledger.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TransactionResponse {
    private Long id;
    private Long bookId;
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private Integer type;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private String remark;
    private String paymentMethod;
    private List<TagInfo> tags;
    private List<String> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class TagInfo {
        private Long id;
        private String name;
        private String color;
    }
}

package com.smartledger.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookResponse {
    private Long id;
    private String name;
    private String icon;
    private String description;
    private Boolean isDefault;
    private Long recordCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

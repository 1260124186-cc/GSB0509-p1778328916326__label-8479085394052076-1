package com.smartledger.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class BookRequest {
    @NotBlank(message = "账本名称不能为空")
    @Size(max = 20, message = "账本名称不能超过20个字符")
    private String name;

    private String icon = "book";

    @Size(max = 200, message = "描述不能超过200个字符")
    private String description;

    private Boolean isDefault = false;
}

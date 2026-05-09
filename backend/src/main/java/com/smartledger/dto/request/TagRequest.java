package com.smartledger.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class TagRequest {
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称不能超过50个字符")
    private String name;

    private String color = "#409EFF";
}

package com.smartledger.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class CategoryRequest {
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称不能超过50个字符")
    private String name;

    private String icon = "tag";

    @NotNull(message = "分类类型不能为空")
    private Integer type;

    private Integer sortOrder = 0;

    private Integer isHidden;

    // Setter to handle Boolean from frontend
    public void setIsHidden(Object isHidden) {
        if (isHidden instanceof Boolean) {
            this.isHidden = ((Boolean) isHidden) ? 1 : 0;
        } else if (isHidden instanceof Integer) {
            this.isHidden = (Integer) isHidden;
        } else if (isHidden instanceof String) {
            this.isHidden = "true".equalsIgnoreCase((String) isHidden) ? 1 : 0;
        }
    }
}

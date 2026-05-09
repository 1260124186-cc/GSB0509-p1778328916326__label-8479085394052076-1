package com.smartledger.dto.request;

import lombok.Data;
import javax.validation.constraints.Size;

@Data
public class UserProfileRequest {
    @Size(max = 50, message = "昵称不能超过50个字符")
    private String nickname;

    private String avatar;
}

package com.smartledger.controller;

import com.smartledger.common.Result;
import com.smartledger.dto.request.ChangePasswordRequest;
import com.smartledger.dto.request.UserProfileRequest;
import com.smartledger.dto.response.UserResponse;
import com.smartledger.exception.BusinessException;
import com.smartledger.security.UserContext;
import com.smartledger.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "获取个人资料")
    public Result<UserResponse> getProfile() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return Result.success(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    @Operation(summary = "更新个人资料")
    public Result<UserResponse> updateProfile(@Valid @RequestBody UserProfileRequest request) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return Result.success(userService.updateProfile(userId, request));
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        userService.changePassword(userId, request);
        return Result.success("密码修改成功", null);
    }

    @PostMapping("/avatar")
    @Operation(summary = "上传头像")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        String avatarUrl = userService.uploadAvatar(userId, file);
        return Result.success("头像上传成功", avatarUrl);
    }
}

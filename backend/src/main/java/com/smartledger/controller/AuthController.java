package com.smartledger.controller;

import com.smartledger.common.Result;
import com.smartledger.dto.request.LoginRequest;
import com.smartledger.dto.request.RegisterRequest;
import com.smartledger.dto.response.LoginResponse;
import com.smartledger.dto.response.UserResponse;
import com.smartledger.security.UserContext;
import com.smartledger.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.success("注册成功", null);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success("登录成功", response);
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息")
    public Result<UserResponse> me() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        UserResponse response = authService.getCurrentUser(userId);
        return Result.success(response);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "找回密码")
    public Result<Void> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        authService.resetPassword(email);
        return Result.success("密码已重置为123456", null);
    }
}

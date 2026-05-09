package com.smartledger.service;

import com.smartledger.dto.request.LoginRequest;
import com.smartledger.dto.request.RegisterRequest;
import com.smartledger.dto.response.LoginResponse;
import com.smartledger.dto.response.UserResponse;

public interface AuthService {
    void register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    UserResponse getCurrentUser(Long userId);
    void resetPassword(String email);
}

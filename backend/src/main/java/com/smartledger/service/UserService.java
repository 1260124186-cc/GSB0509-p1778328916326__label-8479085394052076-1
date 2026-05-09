package com.smartledger.service;

import com.smartledger.dto.request.ChangePasswordRequest;
import com.smartledger.dto.request.UserProfileRequest;
import com.smartledger.dto.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserResponse getProfile(Long userId);
    UserResponse updateProfile(Long userId, UserProfileRequest request);
    void changePassword(Long userId, ChangePasswordRequest request);
    String uploadAvatar(Long userId, MultipartFile file);
}

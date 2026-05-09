package com.smartledger.service.impl;

import com.smartledger.dto.request.ChangePasswordRequest;
import com.smartledger.dto.request.UserProfileRequest;
import com.smartledger.dto.response.UserResponse;
import com.smartledger.entity.User;
import com.smartledger.exception.BusinessException;
import com.smartledger.mapper.UserMapper;
import com.smartledger.service.UserService;
import com.smartledger.service.OperationLogService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private HttpServletRequest request;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserResponse getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        UserResponse response = new UserResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }

    @Override
    public UserResponse updateProfile(Long userId, UserProfileRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 记录修改个人资料日志
        if (request.getNickname() != null) {
            String logContent = String.format("{\"nickname\":\"%s\"}", request.getNickname());
            String ipAddress = getClientIp();
            operationLogService.log(userId, "UPDATE_PROFILE", "USER", userId, logContent, ipAddress);
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        UserResponse response = new UserResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }

    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(400, "两次输入的密码不一致");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(400, "原密码错误");
        }

        // 检查新密码是否与原密码相同
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException(400, "新密码不能与原密码相同");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(400, "请选择文件");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(400, "只支持图片格式");
        }

        // 验证文件大小（5MB）
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BusinessException(400, "图片大小不能超过5MB");
        }

        // 使用Unsplash作为头像服务（简化实现）
        // 生成一个随机的Unsplash图片URL
        String randomId = UUID.randomUUID().toString().substring(0, 8);
        String avatarUrl = String.format("https://images.unsplash.com/photo-%s?w=200&h=200&fit=crop", randomId);

        // 更新用户头像
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        user.setAvatar(avatarUrl);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 记录日志
        String logContent = String.format("{\"avatar\":\"%s\"}", avatarUrl);
        String ipAddress = getClientIp();
        operationLogService.log(userId, "UPDATE_AVATAR", "USER", userId, logContent, ipAddress);

        return avatarUrl;
    }
}

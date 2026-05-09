package com.smartledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartledger.dto.request.LoginRequest;
import com.smartledger.dto.request.RegisterRequest;
import com.smartledger.dto.response.LoginResponse;
import com.smartledger.dto.response.UserResponse;
import com.smartledger.entity.User;
import com.smartledger.entity.Book;
import com.smartledger.exception.BusinessException;
import com.smartledger.mapper.UserMapper;
import com.smartledger.mapper.BookMapper;
import com.smartledger.security.JwtTokenProvider;
import com.smartledger.service.AuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(400, "两次输入的密码不一致");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, request.getEmail());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "该邮箱已被注册");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getEmail().split("@")[0]);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        Book book = new Book();
        book.setUserId(user.getId());
        book.setName("我的账本");
        book.setIcon("book");
        book.setIsDefault(1);
        book.setCreatedAt(LocalDateTime.now());
        book.setUpdatedAt(LocalDateTime.now());
        bookMapper.insert(book);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, request.getEmail());
        User user = userMapper.selectOne(wrapper);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(400, "邮箱或密码错误");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        UserResponse userResponse = new UserResponse();
        BeanUtils.copyProperties(user, userResponse);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(userResponse);
        return response;
    }

    @Override
    public UserResponse getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        UserResponse response = new UserResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }

    @Override
    public void resetPassword(String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(404, "该邮箱未注册");
        }

        user.setPassword(passwordEncoder.encode("123456"));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }
}

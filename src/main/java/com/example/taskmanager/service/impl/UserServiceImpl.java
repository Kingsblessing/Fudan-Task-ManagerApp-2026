package com.example.taskmanager.service.impl;

import com.example.taskmanager.common.ResultCode;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.exception.BusinessException;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户认证服务实现
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User authenticate(Long userId, String rawPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND.code, "用户名或密码错误"));

        if (rawPassword == null || !passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.code, "用户名或密码错误");
        }

        return user;
    }
}

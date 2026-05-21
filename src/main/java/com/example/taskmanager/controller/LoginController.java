package com.example.taskmanager.controller;

import com.example.taskmanager.common.Result;
import com.example.taskmanager.common.ResultCode;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.exception.BusinessException;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 登录接口（模拟登录，无真实认证）
 */
@RestController
@RequestMapping("/api")
public class LoginController {

    private final UserRepository userRepository;

    public LoginController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, Object> req) {
        Number userIdNum = (Number) req.get("userId");
        String role = (String) req.get("role");

        if (userIdNum == null || role == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.code, "userId 和 role 不能为空");
        }

        Long userId = userIdNum.longValue();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND.code, ResultCode.USER_NOT_FOUND.msg));

        if (!user.getRole().name().equals(role)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.code, "用户角色不匹配");
        }

        return Result.success(Map.of(
                "userId", user.getId(),
                "name", user.getName(),
                "role", user.getRole().name()
        ));
    }
}

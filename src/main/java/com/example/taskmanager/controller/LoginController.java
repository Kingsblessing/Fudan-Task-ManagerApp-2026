package com.example.taskmanager.controller;

import com.example.taskmanager.common.*;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.exception.BusinessException;
import com.example.taskmanager.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 登录接口（密码验证 + JWT 双 Token 签发）
 */
@RestController
@RequestMapping("/api")
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public LoginController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, Object> req, HttpServletResponse response) {
        Number userIdNum = (Number) req.get("userId");
        String password = (String) req.get("password");
        String role = (String) req.get("role");

        if (userIdNum == null || password == null || role == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.code, "userId、password 和 role 不能为空");
        }

        Long userId = userIdNum.longValue();
        log.info("用户登录请求: userId={}", userId);

        // 验证密码
        User user = userService.authenticate(userId, password);

        // 校验角色匹配
        if (!user.getRole().name().equals(role)) {
            log.warn("登录失败: userId={}, 角色不匹配", userId);
            throw new BusinessException(ResultCode.UNAUTHORIZED.code, "用户名或密码错误");
        }

        // 签发双 Token，通过 httpOnly Cookie 下发
        String accessToken = jwtUtil.generateAccessToken(userId, role);
        String refreshToken = jwtUtil.generateRefreshToken(userId, role);

        Cookie accessCookie = createTokenCookie("access_token", accessToken, 900);       // 15分钟
        Cookie refreshCookie = createTokenCookie("refresh_token", refreshToken, 604800); // 7天
        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        log.info("登录成功: userId={}, role={}", userId, role);
        return Result.success(Map.of(
                "userId", user.getId(),
                "name", user.getName(),
                "role", user.getRole().name()
        ));
    }

    private Cookie createTokenCookie(String name, String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        cookie.setAttribute("SameSite", "Strict");
        return cookie;
    }
}

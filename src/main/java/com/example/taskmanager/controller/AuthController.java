package com.example.taskmanager.controller;

import com.example.taskmanager.common.*;
import com.example.taskmanager.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 认证控制器：Token 刷新、登出
 */
@RestController
@RequestMapping("/api")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final JwtUtil jwtUtil;
    private final TokenBlacklist tokenBlacklist;

    public AuthController(JwtUtil jwtUtil, TokenBlacklist tokenBlacklist) {
        this.jwtUtil = jwtUtil;
        this.tokenBlacklist = tokenBlacklist;
    }

    /**
     * 刷新 Access Token
     * POST /api/refresh
     */
    @PostMapping("/refresh")
    public Result<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookie(request, "refresh_token");
        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.code, "Refresh Token 无效或已过期");
        }

        String type = jwtUtil.getType(refreshToken);
        if (!"refresh".equals(type)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.code, "Token 类型错误");
        }

        String jti = jwtUtil.getJti(refreshToken);
        if (tokenBlacklist.isBlacklisted(jti)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.code, "Token 已注销");
        }

        Long userId = jwtUtil.getUserId(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        // 签发新的 Access Token
        String newAccessToken = jwtUtil.generateAccessToken(userId, role);
        Cookie accessCookie = createTokenCookie("access_token", newAccessToken, (int) (900)); // 15分钟
        response.addCookie(accessCookie);

        log.info("Token 刷新成功: userId={}", userId);
        return Result.success("Token 刷新成功");
    }

    /**
     * 用户登出
     * POST /api/logout
     */
    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = extractCookie(request, "access_token");
        String refreshToken = extractCookie(request, "refresh_token");

        // 将 Access Token 加入黑名单
        if (accessToken != null) {
            try {
                String jti = jwtUtil.getJti(accessToken);
                long exp = jwtUtil.parseToken(accessToken).getExpiration().getTime();
                tokenBlacklist.blacklist(jti, exp);
            } catch (Exception ignored) {
                // Token 格式错误，忽略
            }
        }

        // 将 Refresh Token 加入黑名单
        if (refreshToken != null) {
            try {
                String jti = jwtUtil.getJti(refreshToken);
                long exp = jwtUtil.parseToken(refreshToken).getExpiration().getTime();
                tokenBlacklist.blacklist(jti, exp);
            } catch (Exception ignored) {
            }
        }

        // 清除 Cookie
        Cookie accessClear = createTokenCookie("access_token", "", 0);
        Cookie refreshClear = createTokenCookie("refresh_token", "", 0);
        response.addCookie(accessClear);
        response.addCookie(refreshClear);

        log.info("用户登出");
        return Result.success("登出成功");
    }

    /**
     * 从请求 Cookie 中提取指定名称的值
     */
    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * 创建 Token Cookie
     */
    private Cookie createTokenCookie(String name, String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        cookie.setAttribute("SameSite", "Strict");
        return cookie;
    }
}

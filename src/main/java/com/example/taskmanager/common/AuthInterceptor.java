package com.example.taskmanager.common;

import com.example.taskmanager.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 权限拦截器（JWT 验证 + 黑名单检查 + RBAC 路径校验）
 */
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);
    private final JwtUtil jwtUtil;
    private final TokenBlacklist tokenBlacklist;

    public AuthInterceptor(JwtUtil jwtUtil, TokenBlacklist tokenBlacklist) {
        this.jwtUtil = jwtUtil;
        this.tokenBlacklist = tokenBlacklist;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();

        // 从 Cookie 中提取 Access Token
        String token = extractCookie(request, "access_token");
        if (token == null) {
            log.warn("鉴权失败: 缺少 token, uri={}", uri);
            throw new BusinessException(ResultCode.UNAUTHORIZED.code, "未登录，请先登录");
        }

        // 校验 Token 有效性
        if (!jwtUtil.validateToken(token)) {
            log.warn("鉴权失败: token 无效或过期, uri={}", uri);
            throw new BusinessException(ResultCode.UNAUTHORIZED.code, "Token 无效或已过期，请重新登录");
        }

        // 检查 Token 类型
        String type = jwtUtil.getType(token);
        if (!"access".equals(type)) {
            log.warn("鉴权失败: token 类型错误, uri={}", uri);
            throw new BusinessException(ResultCode.UNAUTHORIZED.code, "Token 类型错误");
        }

        // 检查黑名单
        String jti = jwtUtil.getJti(token);
        if (tokenBlacklist.isBlacklisted(jti)) {
            log.warn("鉴权失败: token 已注销, uri={}, jti={}", uri, jti);
            throw new BusinessException(ResultCode.UNAUTHORIZED.code, "Token 已注销，请重新登录");
        }

        // 提取用户信息写入上下文
        Long userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);
        UserContext.set(userId, role);

        log.debug("鉴权拦截: uri={}, userId={}, role={}", uri, userId, role);

        // RBAC 路径校验
        if (uri.startsWith("/api/leader") && !"LEADER".equals(role)) {
            log.warn("权限拒绝: userId={}, role={}, uri={}", userId, role, uri);
            throw new BusinessException(ResultCode.UNAUTHORIZED.code, ResultCode.UNAUTHORIZED.msg);
        }
        if (uri.startsWith("/api/worker") && !"WORKER".equals(role)) {
            log.warn("权限拒绝: userId={}, role={}, uri={}", userId, role, uri);
            throw new BusinessException(ResultCode.UNAUTHORIZED.code, ResultCode.UNAUTHORIZED.msg);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}

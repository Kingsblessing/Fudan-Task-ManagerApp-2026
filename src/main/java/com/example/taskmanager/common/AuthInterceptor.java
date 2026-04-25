package com.example.taskmanager.common;

import com.example.taskmanager.exception.BusinessException;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 权限拦截器（校验用户身份&角色权限）
 */
public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userIdStr = request.getHeader("X-User-Id");
        String userRole = request.getHeader("X-User-Role");

        if (userIdStr == null || userRole == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.code, "缺少认证请求头 X-User-Id 或 X-User-Role");
        }

        Long userId;
        try {
            userId = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.PARAM_ERROR.code, "X-User-Id 格式不正确");
        }
        UserContext.set(userId, userRole);

        // RBAC路径校验
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/leader") && !"LEADER".equals(userRole)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.code, ResultCode.UNAUTHORIZED.msg);
        }
        if (uri.startsWith("/api/worker") && !"WORKER".equals(userRole)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.code, ResultCode.UNAUTHORIZED.msg);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}

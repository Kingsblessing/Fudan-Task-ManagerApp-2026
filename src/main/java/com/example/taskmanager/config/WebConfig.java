package com.example.taskmanager.config;

import com.example.taskmanager.common.AuthInterceptor;
import com.example.taskmanager.common.JwtUtil;
import com.example.taskmanager.common.TokenBlacklist;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置（注册拦截器）
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtUtil jwtUtil;
    private final TokenBlacklist tokenBlacklist;

    public WebConfig(JwtUtil jwtUtil, TokenBlacklist tokenBlacklist) {
        this.jwtUtil = jwtUtil;
        this.tokenBlacklist = tokenBlacklist;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor(jwtUtil, tokenBlacklist))
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/login", "/api/refresh", "/api/logout");
    }
}

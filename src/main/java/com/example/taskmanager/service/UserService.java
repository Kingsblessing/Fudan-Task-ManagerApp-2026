package com.example.taskmanager.service;

import com.example.taskmanager.entity.User;

/**
 * 用户认证服务接口
 */
public interface UserService {

    /**
     * 验证用户密码
     * @param userId 用户ID
     * @param rawPassword 明文密码
     * @return 验证通过的用户信息
     */
    User authenticate(Long userId, String rawPassword);
}

package com.example.taskmanager.repository;

import com.example.taskmanager.entity.Role;
import com.example.taskmanager.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * 用户仓储接口（定义数据访问规范）
 */
public interface UserRepository {
    Optional<User> findById(Long id);
    List<User> findAll();
    List<User> findByRole(Role role);
}

package com.example.taskmanager.repository.impl;

import com.example.taskmanager.entity.Role;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户仓储-内存实现（预置种子数据）
 * 当配置文件中storage.type=memory时生效（默认生效）
 */
@Repository
@ConditionalOnProperty(name = "storage.type", havingValue = "memory", matchIfMissing = true)
public class MemoryUserRepositoryImpl implements UserRepository {
    private final Map<Long, User> store = new ConcurrentHashMap<>();

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();
    private static final String DEFAULT_PASSWORD = "password123";

    public MemoryUserRepositoryImpl() {
        String hashed = ENCODER.encode(DEFAULT_PASSWORD);
        seed(1001L, "Leader-张三", Role.LEADER, hashed);
        seed(1002L, "Leader-李四", Role.LEADER, hashed);
        seed(2001L, "Worker-王五", Role.WORKER, hashed);
        seed(2002L, "Worker-赵六", Role.WORKER, hashed);
        seed(2003L, "Worker-钱七", Role.WORKER, hashed);
        seed(2004L, "Worker-孙八", Role.WORKER, hashed);
        seed(2005L, "Worker-周九", Role.WORKER, hashed);
    }

    private void seed(Long id, String name, Role role, String hashedPassword) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setRole(role);
        user.setPassword(hashedPassword);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        store.put(id, user);
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<User> findByRole(Role role) {
        return store.values().stream()
                .filter(u -> u.getRole() == role)
                .toList();
    }
}

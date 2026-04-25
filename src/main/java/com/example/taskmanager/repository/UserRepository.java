package com.example.taskmanager.repository;

import com.example.taskmanager.entity.Role;
import com.example.taskmanager.entity.User;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户仓储（内存实现，预置种子数据）
 */
@Repository
public class UserRepository {
    private final Map<Long, User> store = new ConcurrentHashMap<>();

    public UserRepository() {
        // 种子数据：2个Leader + 5个Worker
        seed(1001L, "Leader-张三", Role.LEADER);
        seed(1002L, "Leader-李四", Role.LEADER);
        seed(2001L, "Worker-王五", Role.WORKER);
        seed(2002L, "Worker-赵六", Role.WORKER);
        seed(2003L, "Worker-钱七", Role.WORKER);
        seed(2004L, "Worker-孙八", Role.WORKER);
        seed(2005L, "Worker-周九", Role.WORKER);
    }

    private void seed(Long id, String name, Role role) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        store.put(id, user);
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<User> findAll() {
        return new ArrayList<>(store.values());
    }

    public List<User> findByRole(Role role) {
        return store.values().stream()
                .filter(u -> u.getRole() == role)
                .toList();
    }
}

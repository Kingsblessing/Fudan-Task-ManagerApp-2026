package com.example.taskmanager.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
public class User {
    private Long id;               // 用户ID
    private String name;           // 用户名称
    private Role role;             // 用户角色（LEADER/WORKER）
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

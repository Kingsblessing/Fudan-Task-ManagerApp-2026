package com.example.taskmanager.repository.impl;

import com.example.taskmanager.entity.Role;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * 用户仓储-数据库实现（基于MySQL + JdbcTemplate）
 * 当配置文件中storage.type=db时生效
 */
@Repository
@ConditionalOnProperty(name = "storage.type", havingValue = "db")
public class DbUserRepositoryImpl implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public DbUserRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM user WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, this::mapRow, id);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    @Override
    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM user", this::mapRow);
    }

    @Override
    public List<User> findByRole(Role role) {
        return jdbcTemplate.query("SELECT * FROM user WHERE role = ?", this::mapRow, role.name());
    }

    private User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setName(rs.getString("name"));
        user.setRole(Role.valueOf(rs.getString("role")));
        user.setPassword(rs.getString("password"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) user.setCreatedAt(created.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) user.setUpdatedAt(updated.toLocalDateTime());
        return user;
    }
}

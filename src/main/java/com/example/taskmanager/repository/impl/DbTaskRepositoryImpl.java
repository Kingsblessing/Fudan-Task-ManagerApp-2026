package com.example.taskmanager.repository.impl;

import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.TaskStatus;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务仓储-数据库实现（基于MySQL + JdbcTemplate）
 * 当配置文件中storage.type=db时生效
 */
@Repository
@ConditionalOnProperty(name = "storage.type", havingValue = "db")
public class DbTaskRepositoryImpl implements TaskRepository {

    private final JdbcTemplate jdbcTemplate;

    public DbTaskRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Task save(Task task) {
        String sql = "INSERT INTO task (title, description, status, creator_id, version) VALUES (?, ?, ?, ?, 1)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getStatus().name());
            ps.setLong(4, task.getCreatorId());
            return ps;
        }, keyHolder);
        task.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        task.setVersion(1);

        // 插入候选工作者
        if (task.getCandidateWorkerIds() != null && !task.getCandidateWorkerIds().isEmpty()) {
            String insertCandidate = "INSERT INTO task_candidate_worker (task_id, worker_id) VALUES (?, ?)";
            for (Long workerId : task.getCandidateWorkerIds()) {
                jdbcTemplate.update(insertCandidate, task.getId(), workerId);
            }
        }

        // 回读时间戳
        jdbcTemplate.query("SELECT created_at, updated_at FROM task WHERE id = ?", rs -> {
            task.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            task.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }, task.getId());

        return task;
    }

    @Override
    public Optional<Task> findById(Long id) {
        String sql = "SELECT * FROM task WHERE id = ?";
        List<Task> tasks = jdbcTemplate.query(sql, this::mapRow, id);
        if (tasks.isEmpty()) {
            return Optional.empty();
        }
        Task task = tasks.get(0);
        loadCandidateWorkers(task);
        return Optional.of(task);
    }

    @Override
    public List<Task> findAll() {
        String sql = "SELECT * FROM task ORDER BY created_at DESC";
        List<Task> tasks = jdbcTemplate.query(sql, this::mapRow);
        // 批量加载候选工作者
        Map<Long, Task> taskMap = tasks.stream().collect(Collectors.toMap(Task::getId, t -> t));
        if (!taskMap.isEmpty()) {
            String inSql = taskMap.keySet().stream().map(String::valueOf).collect(Collectors.joining(","));
            jdbcTemplate.query(
                    "SELECT task_id, worker_id FROM task_candidate_worker WHERE task_id IN (" + inSql + ")",
                    rs -> {
                        Task t = taskMap.get(rs.getLong("task_id"));
                        if (t != null) {
                            if (t.getCandidateWorkerIds() == null) {
                                t.setCandidateWorkerIds(new ArrayList<>());
                            }
                            t.getCandidateWorkerIds().add(rs.getLong("worker_id"));
                        }
                    }
            );
        }
        return tasks;
    }

    @Override
    public int update(Task task) {
        String sql = "UPDATE task SET status=?, assignee_id=?, error_message=?, version=version+1, updated_at=NOW() WHERE id=? AND version=?";
        return jdbcTemplate.update(sql,
                task.getStatus().name(),
                task.getAssigneeId(),
                task.getErrorMessage(),
                task.getId(),
                task.getVersion()
        );
    }

    private Task mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        Task task = new Task();
        task.setId(rs.getLong("id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setStatus(TaskStatus.valueOf(rs.getString("status")));
        task.setCreatorId(rs.getLong("creator_id"));
        long assigneeId = rs.getLong("assignee_id");
        task.setAssigneeId(rs.wasNull() ? null : assigneeId);
        task.setErrorMessage(rs.getString("error_message"));
        task.setVersion(rs.getInt("version"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) task.setCreatedAt(created.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) task.setUpdatedAt(updated.toLocalDateTime());
        return task;
    }

    private void loadCandidateWorkers(Task task) {
        List<Long> ids = jdbcTemplate.query(
                "SELECT worker_id FROM task_candidate_worker WHERE task_id = ?",
                (rs, rowNum) -> rs.getLong("worker_id"),
                task.getId()
        );
        task.setCandidateWorkerIds(ids);
    }
}

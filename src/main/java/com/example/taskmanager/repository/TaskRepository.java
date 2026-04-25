package com.example.taskmanager.repository;
import com.example.taskmanager.entity.Task;
import java.util.List;
import java.util.Optional;

/**
 * 任务仓储接口（定义数据访问规范）
 */
public interface TaskRepository {
    /**
     * 新增任务
     */
    Task save(Task task);
    /**
     * 根据ID查询任务
     */
    Optional<Task> findById(Long id);
    /**
     * 查询所有任务
     */
    List<Task> findAll();
    /**
     * 更新任务（返回受影响行数，用于乐观锁校验）
     */
    int update(Task task);
}
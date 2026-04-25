package com.example.taskmanager.repository.impl;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 任务仓储-内存实现（基于ConcurrentHashMap）
 * 当配置文件中storage.type=memory时生效（默认生效）
 */
@Repository
@ConditionalOnProperty(name = "storage.type", havingValue = "memory", matchIfMissing = true)
public class MemoryTaskRepositoryImpl implements TaskRepository {
    // 内存存储容器（线程安全）
    private final Map<Long, Task> store = new ConcurrentHashMap<>();
    // 自增ID生成器（线程安全）
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Task save(Task task) {
        task.setId(idGenerator.getAndIncrement());
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setVersion(1); // 初始版本号为1
        store.put(task.getId(), task);
        return task;
    }

    @Override
    public Optional<Task> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Task> findAll() {
        return new ArrayList<>(store.values());
    }

    /**
     * 乐观锁更新：仅当版本号匹配时更新
     */
    @Override
    public synchronized int update(Task task) {
        Task existing = store.get(task.getId());
        if (existing != null && existing.getVersion().equals(task.getVersion())) {
            task.setVersion(task.getVersion() + 1); // 版本号自增
            task.setUpdatedAt(LocalDateTime.now());
            store.put(task.getId(), task);
            return 1; // 更新成功（受影响行数=1）
        }
        return 0; // 更新失败（版本冲突/任务不存在）
    }
}
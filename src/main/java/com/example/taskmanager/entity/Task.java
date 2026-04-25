package com.example.taskmanager.entity;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务实体
 */
@Data
public class Task {
    private Long id;                     // 任务ID
    private String title;                // 任务标题
    private String description;          // 任务描述
    private TaskStatus status;           // 任务状态
    private Long creatorId;              // 创建人ID（管理员）
    private Long assigneeId;             // 执行人ID（工作者）
    private String errorMessage;         // 异常信息（暂停/失败时）
    private List<Long> candidateWorkerIds; // 候选执行人ID列表
    private LocalDateTime createdAt;     // 创建时间
    private LocalDateTime updatedAt;     // 更新时间
    private Integer version;             // 版本号（用于乐观锁，解决异步冲突）
}
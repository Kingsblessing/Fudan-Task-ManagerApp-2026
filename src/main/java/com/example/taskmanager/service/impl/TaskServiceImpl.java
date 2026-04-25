package com.example.taskmanager.service.impl;

import com.example.taskmanager.common.ResultCode;
import com.example.taskmanager.common.UserContext;
import com.example.taskmanager.entity.Role;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.TaskStatus;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.exception.BusinessException;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.service.TaskService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务服务实现（核心业务逻辑）
 */
@Service
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskServiceImpl(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    // ==================== Leader ====================

    @Override
    public Task createTask(String title, String description, List<Long> candidateWorkerIds) {
        if (candidateWorkerIds == null || candidateWorkerIds.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.code, "至少需指定一名候选工作者");
        }
        if (title == null || title.isBlank()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.code, "标题不能为空");
        }
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setCreatorId(UserContext.getUserId());
        task.setCandidateWorkerIds(candidateWorkerIds);
        task.setStatus(TaskStatus.PENDING);
        return taskRepository.save(task);
    }

    @Override
    public Task pauseTask(Long taskId) {
        Task task = getTaskById(taskId);
        if (task.getStatus() != TaskStatus.PENDING && task.getStatus() != TaskStatus.IN_PROGRESS) {
            throw new BusinessException(ResultCode.STATUS_INVALID.code, "仅待执行/执行中的任务可暂停");
        }
        task.setStatus(TaskStatus.PAUSED);
        updateWithLocking(task);
        return task;
    }

    @Override
    public Task resumeTask(Long taskId) {
        Task task = getTaskById(taskId);
        if (task.getStatus() != TaskStatus.PAUSED && task.getStatus() != TaskStatus.ERROR_PAUSED) {
            throw new BusinessException(ResultCode.STATUS_INVALID.code, "仅暂停/错误暂停状态的任务可恢复");
        }
        task.setStatus(TaskStatus.PENDING);
        task.setAssigneeId(null);
        task.setErrorMessage(null);
        updateWithLocking(task);
        return task;
    }

    @Override
    public List<Task> getLeaderTaskList(String status, String keyword, Long workerId) {
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
                .filter(t -> status == null || status.isBlank() || t.getStatus().name().equalsIgnoreCase(status))
                .filter(t -> keyword == null || keyword.isBlank()
                        || (t.getTitle() != null && t.getTitle().contains(keyword))
                        || (t.getDescription() != null && t.getDescription().contains(keyword)))
                .filter(t -> workerId == null
                        || (t.getAssigneeId() != null && t.getAssigneeId().equals(workerId))
                        || (t.getCandidateWorkerIds() != null && t.getCandidateWorkerIds().contains(workerId)))
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getWorkerList() {
        List<Task> allTasks = taskRepository.findAll();
        // 按 assigneeId 分组统计
        Map<Long, List<Task>> assigneeMap = allTasks.stream()
                .filter(t -> t.getAssigneeId() != null)
                .collect(Collectors.groupingBy(Task::getAssigneeId));

        List<User> workers = userRepository.findByRole(Role.WORKER);
        List<Map<String, Object>> result = new ArrayList<>();
        for (User w : workers) {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("workerId", w.getId());
            info.put("workerName", w.getName());
            // 被指派任务数：在候选列表中的任务总数
            long assigned = allTasks.stream()
                    .filter(t -> t.getCandidateWorkerIds() != null && t.getCandidateWorkerIds().contains(w.getId()))
                    .count();
            info.put("assignedCount", assigned);
            List<Task> myTasks = assigneeMap.getOrDefault(w.getId(), List.of());
            info.put("processingCount", myTasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count());
            info.put("completedCount", myTasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count());
            result.add(info);
        }
        return result;
    }

    // ==================== Worker ====================

    @Override
    public List<Task> getWorkerTaskList(String type) {
        Long currentUserId = UserContext.getUserId();
        List<Task> tasks = taskRepository.findAll();
        if (type == null || type.isBlank()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.code, "type参数必填（assigned/processing/claimable）");
        }
        return switch (type) {
            case "assigned" -> tasks.stream()
                    .filter(t -> t.getCandidateWorkerIds() != null && t.getCandidateWorkerIds().contains(currentUserId))
                    .collect(Collectors.toList());
            case "processing" -> tasks.stream()
                    .filter(t -> t.getAssigneeId() != null && t.getAssigneeId().equals(currentUserId)
                            && t.getStatus() == TaskStatus.IN_PROGRESS)
                    .collect(Collectors.toList());
            case "claimable" -> tasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.PENDING
                            && t.getCandidateWorkerIds() != null
                            && t.getCandidateWorkerIds().contains(currentUserId)
                            && t.getAssigneeId() == null)
                    .collect(Collectors.toList());
            default -> throw new BusinessException(ResultCode.PARAM_ERROR.code, "type参数值无效，可选值：assigned/processing/claimable");
        };
    }

    @Override
    public Task claimTask(Long taskId) {
        Task task = getTaskById(taskId);
        Long currentUserId = UserContext.getUserId();

        if (task.getStatus() != TaskStatus.PENDING) {
            throw new BusinessException(ResultCode.STATUS_INVALID.code, "任务非待执行状态");
        }
        if (task.getCandidateWorkerIds() == null || !task.getCandidateWorkerIds().contains(currentUserId)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.code, "你不是该任务的候选执行人");
        }
        if (task.getAssigneeId() != null) {
            throw new BusinessException(ResultCode.STATUS_CONFLICT.code, "任务已被认领");
        }

        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setAssigneeId(currentUserId);
        updateWithLocking(task);
        return task;
    }

    @Override
    public Task finishTask(Long taskId) {
        Task task = getTaskById(taskId);
        verifyWorkerOwnership(task);
        if (task.getStatus() != TaskStatus.IN_PROGRESS) {
            throw new BusinessException(ResultCode.STATUS_INVALID.code, "仅执行中的任务可完成");
        }
        task.setStatus(TaskStatus.COMPLETED);
        updateWithLocking(task);
        return task;
    }

    @Override
    public Task errorPauseTask(Long taskId, String errorMessage) {
        Task task = getTaskById(taskId);
        verifyWorkerOwnership(task);
        if (task.getStatus() != TaskStatus.IN_PROGRESS) {
            throw new BusinessException(ResultCode.STATUS_INVALID.code, "仅执行中的任务可上报错误");
        }
        if (errorMessage == null || errorMessage.isBlank()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.code, "errorMessage不能为空");
        }
        task.setStatus(TaskStatus.ERROR_PAUSED);
        task.setErrorMessage(errorMessage);
        updateWithLocking(task);
        return task;
    }

    // ==================== Common ====================

    @Override
    public Task getTaskDetail(Long taskId) {
        Task task = getTaskById(taskId);
        String role = UserContext.getUserRole();
        Long currentUserId = UserContext.getUserId();

        if ("LEADER".equals(role)) {
            return task;
        }
        // Worker: 至少可查看自己在候选列表中或已认领的任务
        boolean isCandidate = task.getCandidateWorkerIds() != null && task.getCandidateWorkerIds().contains(currentUserId);
        boolean isAssignee = currentUserId.equals(task.getAssigneeId());
        if (!isCandidate && !isAssignee) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.code, "无权查看该任务");
        }
        return task;
    }

    // ==================== Helpers ====================

    private void verifyWorkerOwnership(Task task) {
        if (!UserContext.getUserId().equals(task.getAssigneeId())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.code, "你不是该任务的执行人");
        }
    }

    private void updateWithLocking(Task task) {
        int rows = taskRepository.update(task);
        if (rows == 0) {
            throw new BusinessException(ResultCode.STATUS_CONFLICT.code, "任务状态已被其他用户修改，请刷新后重试");
        }
    }

    private Task getTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(ResultCode.TASK_NOT_FOUND.code, ResultCode.TASK_NOT_FOUND.msg));
    }
}

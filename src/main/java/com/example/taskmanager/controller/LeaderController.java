package com.example.taskmanager.controller;

import com.example.taskmanager.aop.annotation.DebugLog;
import com.example.taskmanager.common.Result;
import com.example.taskmanager.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员接口控制器
 */
@RestController
@RequestMapping("/api/leader")
public class LeaderController {

    private final TaskService taskService;

    public LeaderController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * 创建任务
     * POST /api/leader/task
     */
    @DebugLog
    @PostMapping("/task")
    public Result<?> createTask(@RequestBody Map<String, Object> req) {
        String title = (String) req.get("title");
        String description = (String) req.get("description");
        @SuppressWarnings("unchecked")
        List<Number> rawIds = (List<Number>) req.get("candidateWorkerIds");
        List<Long> workerIds = rawIds.stream().map(Number::longValue).toList();
        return Result.success(taskService.createTask(title, description, workerIds));
    }

    /**
     * 查看全部任务（支持筛选）
     * GET /api/leader/tasks?status=&keyword=&workerId=
     */
    @DebugLog
    @GetMapping("/tasks")
    public Result<?> getTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long workerId) {
        return Result.success(taskService.getLeaderTaskList(status, keyword, workerId));
    }

    /**
     * 暂停任务
     * POST /api/leader/tasks/{taskId}/pause
     */
    @DebugLog
    @PostMapping("/tasks/{taskId}/pause")
    public Result<?> pauseTask(@PathVariable Long taskId) {
        return Result.success(taskService.pauseTask(taskId));
    }

    /**
     * 恢复任务
     * POST /api/leader/tasks/{taskId}/resume
     */
    @DebugLog
    @PostMapping("/tasks/{taskId}/resume")
    public Result<?> resumeTask(@PathVariable Long taskId) {
        return Result.success(taskService.resumeTask(taskId));
    }

    /**
     * 查看Worker列表
     * GET /api/leader/workers
     */
    @DebugLog
    @GetMapping("/workers")
    public Result<?> getWorkers() {
        return Result.success(taskService.getWorkerList());
    }
}

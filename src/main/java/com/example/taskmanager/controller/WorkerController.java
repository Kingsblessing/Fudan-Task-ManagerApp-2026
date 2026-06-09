package com.example.taskmanager.controller;

import com.example.taskmanager.aop.annotation.DebugLog;
import com.example.taskmanager.common.Result;
import com.example.taskmanager.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 工作者接口控制器
 */
@RestController
@RequestMapping("/api/worker")
public class WorkerController {

    private final TaskService taskService;

    public WorkerController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * 查看任务列表
     * GET /api/worker/tasks?type=assigned|processing|claimable
     */
    @DebugLog
    @GetMapping("/tasks")
    public Result<?> getTasks(@RequestParam String type) {
        return Result.success(taskService.getWorkerTaskList(type));
    }

    /**
     * 认领任务
     * POST /api/worker/tasks/{taskId}/claim
     */
    @DebugLog
    @PostMapping("/tasks/{taskId}/claim")
    public Result<?> claimTask(@PathVariable Long taskId) {
        return Result.success(taskService.claimTask(taskId));
    }

    /**
     * 完成任务
     * POST /api/worker/tasks/{taskId}/finish
     */
    @DebugLog
    @PostMapping("/tasks/{taskId}/finish")
    public Result<?> finishTask(@PathVariable Long taskId) {
        return Result.success(taskService.finishTask(taskId));
    }

    /**
     * 错误暂停任务
     * POST /api/worker/tasks/{taskId}/error-pause
     */
    @DebugLog
    @PostMapping("/tasks/{taskId}/error-pause")
    public Result<?> errorPauseTask(@PathVariable Long taskId, @RequestBody Map<String, String> req) {
        String errorMessage = req.get("errorMessage");
        return Result.success(taskService.errorPauseTask(taskId, errorMessage));
    }
}

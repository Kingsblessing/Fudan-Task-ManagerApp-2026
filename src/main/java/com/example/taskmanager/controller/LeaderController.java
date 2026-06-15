package com.example.taskmanager.controller;

import com.example.taskmanager.aop.annotation.DebugLog;
import com.example.taskmanager.common.Result;
import com.example.taskmanager.common.UserContext;
import com.example.taskmanager.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员接口控制器
 */
@RestController
@RequestMapping("/api/leader")
public class LeaderController {

    private static final Logger log = LoggerFactory.getLogger(LeaderController.class);
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
        log.info("创建任务请求: title={}, candidateWorkerIds={}", title, workerIds);
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
        log.debug("查询任务列表: status={}, keyword={}, workerId={}", status, keyword, workerId);
        return Result.success(taskService.getLeaderTaskList(status, keyword, workerId));
    }

    /**
     * 暂停任务
     * POST /api/leader/tasks/{taskId}/pause
     */
    @DebugLog
    @PostMapping("/tasks/{taskId}/pause")
    public Result<?> pauseTask(@PathVariable Long taskId) {
        log.info("暂停任务: taskId={}, leaderId={}", taskId, UserContext.getUserId());
        return Result.success(taskService.pauseTask(taskId));
    }

    /**
     * 恢复任务
     * POST /api/leader/tasks/{taskId}/resume
     */
    @DebugLog
    @PostMapping("/tasks/{taskId}/resume")
    public Result<?> resumeTask(@PathVariable Long taskId) {
        log.info("恢复任务: taskId={}, leaderId={}", taskId, UserContext.getUserId());
        return Result.success(taskService.resumeTask(taskId));
    }

    /**
     * 查看Worker列表
     * GET /api/leader/workers
     */
    @DebugLog
    @GetMapping("/workers")
    public Result<?> getWorkers() {
        log.debug("查询 Worker 列表");
        return Result.success(taskService.getWorkerList());
    }
}

package com.example.taskmanager.controller;

import com.example.taskmanager.aop.annotation.DebugLog;
import com.example.taskmanager.common.Result;
import com.example.taskmanager.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * 公共接口控制器
 */
@RestController
@RequestMapping("/api")
public class CommonController {

    private static final Logger log = LoggerFactory.getLogger(CommonController.class);
    private final TaskService taskService;

    public CommonController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * 查看任务详情
     * GET /api/tasks/{taskId}
     */
    @DebugLog
    @GetMapping("/tasks/{taskId}")
    public Result<?> getTaskDetail(@PathVariable Long taskId) {
        log.debug("查询任务详情: taskId={}", taskId);
        return Result.success(taskService.getTaskDetail(taskId));
    }
}

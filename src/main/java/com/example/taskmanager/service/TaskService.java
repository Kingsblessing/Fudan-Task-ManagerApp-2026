package com.example.taskmanager.service;

import com.example.taskmanager.entity.Task;
import java.util.List;
import java.util.Map;

/**
 * 任务服务接口
 */
public interface TaskService {
    // Leader
    Task createTask(String title, String description, List<Long> candidateWorkerIds);
    Task pauseTask(Long taskId);
    Task resumeTask(Long taskId);
    List<Task> getLeaderTaskList(String status, String keyword, Long workerId);
    List<Map<String, Object>> getWorkerList();

    // Worker
    List<Task> getWorkerTaskList(String type);
    Task claimTask(Long taskId);
    Task finishTask(Long taskId);
    Task errorPauseTask(Long taskId, String errorMessage);

    // Common
    Task getTaskDetail(Long taskId);
}

package com.example.taskmanager.entity;

/**
 * 任务状态枚举
 */
public enum TaskStatus {
    PENDING,        // 待处理
    IN_PROGRESS,    // 进行中
    PAUSED,         // 已暂停（主动）
    ERROR_PAUSED,   // 已暂停（异常）
    COMPLETED       // 已完成
}
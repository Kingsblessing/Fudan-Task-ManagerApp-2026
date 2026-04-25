package com.example.taskmanager.common;

/**
 * 响应码枚举
 */
public enum ResultCode {
    SUCCESS(0, "成功"),
    PARAM_ERROR(4001, "参数错误"),
    TASK_NOT_FOUND(4002, "任务不存在"),
    STATUS_INVALID(4003, "任务状态非法"),
    STATUS_CONFLICT(4004, "任务状态冲突"),
    UNAUTHORIZED(4005, "权限不足"),
    USER_NOT_FOUND(4006, "用户不存在"),
    SERVER_ERROR(5000, "服务器内部错误");
    
    public final int code;   // 错误码
    public final String msg; // 错误描述
    ResultCode(int code, String msg) { this.code = code; this.msg = msg; }
}
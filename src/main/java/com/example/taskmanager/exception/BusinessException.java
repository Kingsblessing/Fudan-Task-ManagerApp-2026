package com.example.taskmanager.exception;

import lombok.Getter;

/**
 * 自定义业务异常（携带业务错误码）
 */
@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}

package com.example.taskmanager.common;
import lombok.Data;

/**
 * 统一响应结果封装
 */
@Data
public class Result<T> {
    private int code;       // 响应码（0=成功，非0=异常）
    private String message; // 响应信息
    private T data;         // 响应数据

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /**
     * 失败响应（自定义错误码+信息）
     */
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}


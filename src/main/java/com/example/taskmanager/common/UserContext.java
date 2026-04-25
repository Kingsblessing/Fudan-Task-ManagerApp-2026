package com.example.taskmanager.common;

/**
 * 用户上下文（基于ThreadLocal，存储当前请求的用户信息）
 */
public class UserContext {
    // 当前用户ID
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    // 当前用户角色
    private static final ThreadLocal<String> USER_ROLE = new ThreadLocal<>();

    /**
     * 设置当前用户上下文
     */
    public static void set(Long id, String role) { USER_ID.set(id); USER_ROLE.set(role); }
    /**
     * 获取当前用户ID
     */
    public static Long getUserId() { return USER_ID.get(); }
    /**
     * 获取当前用户角色
     */
    public static String getUserRole() { return USER_ROLE.get(); }
    /**
     * 清理上下文（防止内存泄漏）
     */
    public static void clear() { USER_ID.remove(); USER_ROLE.remove(); }
}
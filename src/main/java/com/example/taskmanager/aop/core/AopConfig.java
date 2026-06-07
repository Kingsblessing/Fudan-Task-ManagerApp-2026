package com.example.taskmanager.aop.core;

/**
 * AOP模式配置。
 * 控制切面工具的工作模式：正常模式（无日志）或Debug模式（打印切面日志）。
 */
public class AopConfig {

    private static volatile boolean debugMode = false;

    /**
     * 开启Debug模式
     */
    public static void enableDebug() {
        debugMode = true;
    }

    /**
     * 关闭Debug模式，切换为正常运行模式
     */
    public static void disableDebug() {
        debugMode = false;
    }

    /**
     * 查询当前是否为Debug模式
     */
    public static boolean isDebug() {
        return debugMode;
    }
}

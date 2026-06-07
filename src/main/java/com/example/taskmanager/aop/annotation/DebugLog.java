package com.example.taskmanager.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Debug日志标记注解。
 * 标注在公共方法上，Debug模式下被标注的方法将自动打印调用日志。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DebugLog {
}

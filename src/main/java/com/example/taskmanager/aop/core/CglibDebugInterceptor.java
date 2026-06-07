package com.example.taskmanager.aop.core;

import com.example.taskmanager.aop.annotation.DebugLog;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 基于CGLIB的方法拦截器。
 * 用于没有实现接口的类（如Controller），通过CGLIB生成子类代理。
 */
public class CglibDebugInterceptor implements MethodInterceptor {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final Object target;

    public CglibDebugInterceptor(Object target) {
        this.target = target;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        DebugLog annotation = method.getAnnotation(DebugLog.class);

        if (annotation != null) {
            String timestamp = LocalDateTime.now().format(FORMATTER);
            String className = target.getClass().getSimpleName();
            String methodName = method.getName();
            String params = buildParamString(method.getParameters(), args);

            System.out.println("========== [DebugLog] ==========");
            System.out.println("  时间: " + timestamp);
            System.out.println("  类名: " + className);
            System.out.println("  方法: " + methodName);
            System.out.println("  参数: " + params);
            System.out.println("=================================");
        }

        return methodProxy.invoke(target, args);
    }

    private String buildParamString(Parameter[] parameters, Object[] args) {
        if (parameters == null || parameters.length == 0) {
            return "无";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(parameters[i].getName()).append("=").append(args[i]);
        }
        return sb.toString();
    }
}

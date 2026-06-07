package com.example.taskmanager.aop.core;

import com.example.taskmanager.aop.annotation.DebugLog;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Debug模式下的方法调用处理器。
 * 对标注了 {@link DebugLog} 的方法进行拦截，自动打印切面日志后放行原始调用。
 */
public class DebugHandler implements InvocationHandler {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final Object target;

    public DebugHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 获取目标类上对应方法的注解（代理接口的方法需要映射到实现类的方法）
        Method targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
        DebugLog annotation = targetMethod.getAnnotation(DebugLog.class);

        if (annotation != null) {
            // 打印切面日志
            String timestamp = LocalDateTime.now().format(FORMATTER);
            String className = target.getClass().getSimpleName();
            String methodName = method.getName();
            String params = buildParamString(targetMethod.getParameters(), args);

            System.out.println("========== [DebugLog] ==========");
            System.out.println("  时间: " + timestamp);
            System.out.println("  类名: " + className);
            System.out.println("  方法: " + methodName);
            System.out.println("  参数: " + params);
            System.out.println("=================================");
        }

        // 通过目标类的方法调用（避免接口方法的访问限制问题）
        targetMethod.setAccessible(true);
        return targetMethod.invoke(target, args);
    }

    /**
     * 拼接参数名=参数值的字符串
     */
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

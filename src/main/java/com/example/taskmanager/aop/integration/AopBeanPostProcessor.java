package com.example.taskmanager.aop.integration;

import com.example.taskmanager.aop.annotation.DebugLog;
import com.example.taskmanager.aop.core.AopConfig;
import com.example.taskmanager.aop.core.AopProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Spring Bean后处理器。
 * 在Bean初始化阶段注入，读取 aop.mode 配置并设置模式开关，
 * 自动为含 @DebugLog 方法的 Bean 创建代理。
 *
 * <p>使用 BeanPostProcessor 而非 ApplicationRunner，确保模式开关在代理创建前生效。</p>
 */
@Component
public class AopBeanPostProcessor implements BeanPostProcessor {

    public AopBeanPostProcessor(@Value("${aop.mode:normal}") String mode) {
        if ("debug".equalsIgnoreCase(mode)) {
            AopConfig.enableDebug();
            System.out.println("[AOP] Debug模式已开启，@DebugLog标注的方法将自动打印切面日志");
        } else {
            AopConfig.disableDebug();
            System.out.println("[AOP] 正常运行模式，切面日志已关闭");
        }
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!AopConfig.isDebug()) {
            return bean;
        }
        if (hasDebugLogMethod(bean)) {
            return AopProxyFactory.createProxy(bean);
        }
        return bean;
    }

    private boolean hasDebugLogMethod(Object bean) {
        for (Method method : bean.getClass().getMethods()) {
            if (method.getAnnotation(DebugLog.class) != null) {
                return true;
            }
        }
        return false;
    }
}

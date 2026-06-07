package com.example.taskmanager.aop.core;

import org.springframework.cglib.proxy.Enhancer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

/**
 * AOP代理工厂。
 * 有业务接口的类使用JDK动态代理，无接口的类（如Controller）使用CGLIB代理。
 */
public class AopProxyFactory {

    @SuppressWarnings("unchecked")
    public static <T> T createProxy(T target) {
        if (!AopConfig.isDebug()) {
            return target;
        }

        Class<?>[] interfaces = target.getClass().getInterfaces();

        // 过滤掉Spring框架自身生成的接口，只保留业务接口
        Class<?>[] businessInterfaces = java.util.Arrays.stream(interfaces)
                .filter(i -> !i.getName().startsWith("org.springframework."))
                .toArray(Class<?>[]::new);

        if (businessInterfaces.length > 0) {
            // 有业务接口 → JDK动态代理
            return (T) Proxy.newProxyInstance(
                    target.getClass().getClassLoader(),
                    businessInterfaces,
                    new DebugHandler(target)
            );
        } else {
            // 无业务接口（如Controller） → CGLIB代理
            return createCglibProxy(target);
        }
    }

    /**
     * 创建CGLIB代理，自动处理有参构造函数的情况。
     * 通过反射从目标实例的字段中提取构造参数值，传给CGLIB的Enhancer。
     */
    @SuppressWarnings("unchecked")
    private static <T> T createCglibProxy(T target) {
        Class<?> targetClass = target.getClass();
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetClass);
        enhancer.setCallback(new CglibDebugInterceptor(target));

        // 查找目标类的构造函数
        Constructor<?>[] constructors = targetClass.getDeclaredConstructors();

        // 优先找无参构造
        for (Constructor<?> ctor : constructors) {
            if (ctor.getParameterCount() == 0) {
                return (T) enhancer.create();
            }
        }

        // 有参构造：取第一个构造函数，从目标实例字段中提取参数值
        Constructor<?> ctor = constructors[0];
        Class<?>[] paramTypes = ctor.getParameterTypes();
        Object[] paramValues = new Object[paramTypes.length];

        for (int i = 0; i < paramTypes.length; i++) {
            paramValues[i] = findFieldValue(target, targetClass, paramTypes[i]);
        }

        return (T) enhancer.create(paramTypes, paramValues);
    }

    /**
     * 从目标实例的字段中查找匹配指定类型的值（含父类字段）
     */
    private static Object findFieldValue(Object target, Class<?> clazz, Class<?> fieldType) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (fieldType.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    try {
                        return field.get(target);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("无法读取字段 " + field.getName() + " 的值", e);
                    }
                }
            }
            current = current.getSuperclass();
        }
        throw new RuntimeException("在 " + clazz.getSimpleName() + " 中找不到类型为 " + fieldType.getSimpleName() + " 的字段");
    }
}

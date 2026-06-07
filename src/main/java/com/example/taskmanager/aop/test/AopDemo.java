package com.example.taskmanager.aop.test;

import com.example.taskmanager.aop.annotation.DebugLog;
import com.example.taskmanager.aop.core.AopConfig;
import com.example.taskmanager.aop.core.AopProxyFactory;

/**
 * AOP切面工具独立测试入口。
 * 演示正常模式与Debug模式下，@DebugLog注解的效果差异。
 */
public class AopDemo {

    // ==================== 测试用业务接口 ====================

    interface Calculator {
        int add(int a, int b);
        int multiply(int a, int b);
        String greet(String name);
    }

    // ==================== 测试用业务实现 ====================

    static class CalculatorImpl implements Calculator {

        @DebugLog
        @Override
        public int add(int a, int b) {
            return a + b;
        }

        // multiply 未标注 @DebugLog，Debug模式下也不应打印日志
        @Override
        public int multiply(int a, int b) {
            return a * b;
        }

        @DebugLog
        @Override
        public String greet(String name) {
            return "Hello, " + name + "!";
        }
    }

    // ==================== 测试入口 ====================

    public static void main(String[] args) {
        CalculatorImpl realCalc = new CalculatorImpl();

        System.out.println("============================================");
        System.out.println("  测试一：正常运行模式（无切面日志）");
        System.out.println("============================================");
        AopConfig.disableDebug();
        Calculator normalProxy = AopProxyFactory.createProxy(realCalc);
        System.out.println("  add(3, 5) = " + normalProxy.add(3, 5));
        System.out.println("  multiply(4, 6) = " + normalProxy.multiply(4, 6));
        System.out.println("  greet(\"World\") = " + normalProxy.greet("World"));
        System.out.println();

        System.out.println("============================================");
        System.out.println("  测试二：Debug调试模式（自动打印切面日志）");
        System.out.println("============================================");
        AopConfig.enableDebug();
        Calculator debugProxy = AopProxyFactory.createProxy(realCalc);

        System.out.println("\n  >>> 调用 add(10, 20) — 标注了 @DebugLog，应打印日志");
        int sum = debugProxy.add(10, 20);
        System.out.println("  返回值: " + sum);

        System.out.println("\n  >>> 调用 multiply(3, 7) — 未标注 @DebugLog，不应打印日志");
        int product = debugProxy.multiply(3, 7);
        System.out.println("  返回值: " + product);

        System.out.println("\n  >>> 调用 greet(\"Fudan\") — 标注了 @DebugLog，应打印日志");
        String msg = debugProxy.greet("Fudan");
        System.out.println("  返回值: " + msg);

        System.out.println("\n============================================");
        System.out.println("  测试完成！两种模式均可正常工作。");
        System.out.println("============================================");
    }
}

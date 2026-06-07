# AOP 切面编程实验报告

## 一、实验概述

本实验在不使用 Spring AOP 或 AspectJ 的前提下，基于 **Java 反射 + JDK 动态代理 + CGLIB** 手动实现了一套简易 AOP 切面工具，并将其集成到 Spring Boot 后端项目中。

## 二、项目结构

```
src/main/java/com/example/taskmanager/
├── aop/
│   ├── annotation/
│   │   └── DebugLog.java              ← 自定义标记注解
│   ├── core/
│   │   ├── AopConfig.java             ← 模式开关（正常/Debug）
│   │   ├── DebugHandler.java          ← JDK动态代理的InvocationHandler
│   │   ├── CglibDebugInterceptor.java ← CGLIB代理的MethodInterceptor
│   │   └── AopProxyFactory.java       ← 代理工厂（自动选择JDK/CGLIB）
│   └── integration/
│       └── AopBeanPostProcessor.java  ← Spring BeanPostProcessor，自动代理+模式初始化
│   └── test/
│       └── AopDemo.java               ← 独立测试入口（不含Spring）
├── controller/
│   └── LeaderController.java          ← 已标注 @DebugLog（CGLIB代理）
└── service/
    └── impl/
        └── TaskServiceImpl.java       ← 已标注 @DebugLog（JDK动态代理）
```

## 三、核心实现说明

### 3.1 自定义注解 `@DebugLog`

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DebugLog {}
```

仅用于标记公共方法。运行时通过反射读取，决定是否对该方法进行切面增强。

### 3.2 模式开关 `AopConfig`

- `enableDebug()` / `disableDebug()` — 切换 Debug / 正常模式
- `isDebug()` — 查询当前模式
- 使用 `volatile` 保证多线程可见性

### 3.3 代理工厂 `AopProxyFactory`

根据目标类是否实现业务接口，自动选择代理方式：

| 条件 | 代理方式 | 示例 |
|------|---------|------|
| 有业务接口 | JDK 动态代理 | `TaskServiceImpl` (implements `TaskService`) |
| 无业务接口 | CGLIB 子类代理 | `LeaderController` (无接口) |

正常模式下直接返回原对象，**没有任何性能损耗**。

### 3.4 切面处理器

**JDK 动态代理 — `DebugHandler`**（实现 `InvocationHandler`）：

1. 通过反射获取目标类上对应方法的 `@DebugLog` 注解
2. 若注解存在 → 打印切面日志（时间、类名、方法名、参数名=参数值）
3. 通过 `targetMethod.invoke(target, args)` 调用原始方法
4. 若注解不存在 → 直接放行，不打印任何日志

**CGLIB 代理 — `CglibDebugInterceptor`**（实现 `MethodInterceptor`）：

逻辑与 `DebugHandler` 一致，通过 `methodProxy.invoke(target, args)` 调用原始方法。
通过反射从目标实例字段中提取构造参数值，解决 CGLIB 代理有参构造类的问题。

### 3.5 Spring 集成

**`AopBeanPostProcessor`**（核心）：

- 实现 `BeanPostProcessor`，在 Bean 初始化后自动检测含 `@DebugLog` 方法的类
- **构造时**即通过 `@Value("${aop.mode:normal}")` 读取配置并设置全局开关（解决时序问题：BeanPostProcessor 先于普通 Bean 创建）
- 为匹配的 Bean 创建代理，**业务代码完全无侵入**

**关键设计：时序保证**

```
Spring 容器启动
  ├─ 1. 创建 BeanPostProcessor（此时读取 aop.mode 配置，设置 AopConfig）
  ├─ 2. 创建业务 Bean（TaskServiceImpl, LeaderController, ...）
  └─ 3. 对每个 Bean 调用 postProcessAfterInitialization()
       ├─ 检查 AopConfig.isDebug() → true
       ├─ 检查是否有 @DebugLog 方法
       └─ 有 → 创建代理替换原 Bean
```

## 四、已标注 `@DebugLog` 的方法

| 类 | 方法 | 功能 | 代理方式 |
|----|------|------|---------|
| `LeaderController` | `createTask` | 创建任务 | CGLIB |
| `LeaderController` | `getTasks` | 查看任务列表 | CGLIB |
| `LeaderController` | `getWorkers` | 查看Worker列表 | CGLIB |
| `TaskServiceImpl` | `createTask` | 创建任务 | JDK动态代理 |
| `TaskServiceImpl` | `claimTask` | 认领任务 | JDK动态代理 |
| `TaskServiceImpl` | `finishTask` | 完成任务 | JDK动态代理 |
| `TaskServiceImpl` | `getTaskDetail` | 查看任务详情 | JDK动态代理 |

所有业务代码仅添加了 `@DebugLog` 注解，**无任何代理相关代码**。

## 五、两种模式演示

### 模式配置

在 `application.yml` 中配置：

```yaml
aop:
  mode: debug    # debug 开启切面日志 | normal 关闭切面日志
```

### 独立测试（`AopDemo.java`）

直接运行 `AopDemo.main()` 可验证两种模式：

**正常模式** — 无任何额外日志输出：

```
============================================
  测试一：正常运行模式（无切面日志）
============================================
  add(3, 5) = 8
  multiply(4, 6) = 24
  greet("World") = Hello, World!
```

**Debug 模式** — 仅 `@DebugLog` 标注的方法打印日志：

```
============================================
  测试二：Debug调试模式（自动打印切面日志）
============================================

  >>> 调用 add(10, 20) — 标注了 @DebugLog，应打印日志
========== [DebugLog] ==========
  时间: 2026-06-07 16:55:06.472
  类名: CalculatorImpl
  方法: add
  参数: a=10, b=20
=================================
  返回值: 30

  >>> 调用 multiply(3, 7) — 未标注 @DebugLog，不应打印日志
  返回值: 21

  >>> 调用 greet("Fudan") — 标注了 @DebugLog，应打印日志
========== [DebugLog] ==========
  时间: 2026-06-07 16:55:06.475
  类名: CalculatorImpl
  方法: greet
  参数: name=Fudan
=================================
  返回值: Hello, Fudan!
```

### Spring Boot 集成测试

```
$ curl -H "X-User-Id: 1" -H "X-User-Role: LEADER" http://localhost:8080/api/leader/workers

========== [DebugLog] ==========
  时间: 2026-06-07 18:08:38.557
  类名: LeaderController
  方法: getWorkers
  参数: 无
=================================
{"code":0,"data":[...],"message":"success"}
```

**验证结论**：
- ✅ 正常模式无任何切面日志
- ✅ Debug 模式下仅 `@DebugLog` 标注的方法打印日志
- ✅ 未标注的方法不打印日志
- ✅ 日志包含：时间、类名、方法名、参数名及参数值
- ✅ 业务代码无任何代理相关代码
- ✅ 支持有接口类（JDK动态代理）和无接口类（CGLIB）

## 六、测试方法

### 6.1 独立测试（无需数据库）

```bash
# 编译
./mvnw compile

# 运行独立测试
./mvnw exec:java -Dexec.mainClass="com.example.taskmanager.aop.test.AopDemo"
```

### 6.2 Spring Boot 集成测试

```bash
# 启动项目（需MySQL，或改为 storage.type: memory）
./mvnw spring-boot:run

# 访问任意标注了 @DebugLog 的 API，观察控制台输出：
curl -H "X-User-Id: 1" -H "X-User-Role: LEADER" http://localhost:8080/api/leader/workers
curl -H "X-User-Id: 1" -H "X-User-Role: LEADER" http://localhost:8080/api/leader/tasks
```

### 6.3 模式切换验证

1. 修改 `application.yml` 中 `aop.mode: debug`，重启，调用接口 → 控制台有切面日志
2. 修改 `application.yml` 中 `aop.mode: normal`，重启，调用接口 → 控制台无切面日志

## 七、设计要点

| 设计点 | 说明 |
|--------|------|
| **零侵入** | 业务类中不出现任何 Proxy/InvocationHandler 代码 |
| **零开销** | 正常模式下 `createProxy` 直接返回原对象 |
| **双代理策略** | 有接口用 JDK 动态代理，无接口用 CGLIB 子类代理 |
| **时序保证** | 模式开关在 BeanPostProcessor 构造时初始化，早于业务 Bean 创建 |
| **注解驱动** | 仅 `@DebugLog` 标注的方法被增强，精确控制 |
| **参数名保留** | `pom.xml` 中配置 `-parameters` 编译标志，保证反射获取真实参数名 |

## 八、关键修复记录

实现过程中解决了以下关键问题：

1. **时序问题**：原设计用 `ApplicationRunner` 初始化模式，但它在所有 Bean 创建之后才执行，导致代理创建时 `isDebug()` 仍为 `false`。修复：将模式初始化移至 `BeanPostProcessor` 构造函数。

2. **接口限制**：Controller 类没有实现接口，JDK 动态代理无法代理。修复：增加 CGLIB 代理路径，通过反射提取构造参数解决有参构造问题。

3. **Lombok + maven-compiler-plugin 冲突**：显式声明编译器插件导致 Lombok 注解处理器失效。修复：改用 `<maven.compiler.parameters>true</maven.compiler.parameters>` 属性配置。

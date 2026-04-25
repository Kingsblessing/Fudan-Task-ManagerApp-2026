# 任务管理系统 (Task-Manager) 后端项目文档

## 一、项目概述

本项目实现了一个支持”角色权限隔离（Leader / Worker）+ 任务全生命周期管理“的后端系统。

系统支持任务创建、认领、执行、暂停、恢复及错误处理，并提供两种存储方式：

- 内存存储（快速开发 / 测试）
- 数据库存储（MySQL + JdbcTemplate）

设计重点在于：高内聚、低耦合、易扩展；设计满足单一职责原则 SRP、开闭原则 OCP。

**技术栈**：Java, Spring Boot, Spring JDBC, MySQL/H2, Lombok, Postman

## 二、项目结构

项目采用分层架构设计，严格遵循 单一职责原则 SRP，确保各层级逻辑清晰：

```
task-manager
├── src/main/java/com/example/taskmanager
│   ├── TaskManagerApplication.java       // 应用启动类
│   ├── common                            // 通用组件
│   │   ├── AuthInterceptor.java          // 解析请求头并校验角色访问路径
│   │   ├── Result.java                   // 统一响应格式
│   │   ├── ResultCode.java               // 错误码字典
│   │   └── UserContext.java              // 当前用户上下文（基于ThreadLocal）
│   ├── config
│   │   └── WebConfig.java                // 注册拦截器
│   │   ├── StorageProperties.java        // 配置类
│   ├── controller                        // 控制器层
│   │   ├── CommonController.java         // 通用接口控制器
│   │   ├── LeaderController.java         // 管理员（负责人）接口控制器
│   │   └── WorkerController.java         // 普通工作者接口控制器
│   ├── entity                            // 领域实体
│   │   ├── Role.java                     // 角色枚举
│   │   ├── Task.java                     // 任务实体
│   │   ├── TaskStatus.java               // 任务状态枚举
│   │   └── User.java                     // 用户实体
│   ├── exception                         // 异常处理
│   │   ├── BusinessException.java        // 自定义业务异常
│   │   └── GlobalExceptionHandler.java   // 全局异常处理器（转换异常为标准JSON响应）
│   ├── repository                        // 仓储层（数据访问）
│   │   ├── TaskRepository.java           // 任务存储抽象接口
│   │   ├── impl
│   │   │   ├── DbTaskRepositoryImpl.java // MySQL实现
│   │   │   └── MemoryTaskRepositoryImpl.java // HashMap内存实现
│   │   └── UserRepository.java           // 用户仓储接口
│   └── service                           // 服务层（业务逻辑）
│       ├── TaskService.java              // 任务服务接口
│       └── impl
│           └── TaskServiceImpl.java      // 核心状态机与业务逻辑实现
├── src/main/resources
│   ├── application.yml                   // 应用配置与存储方式切换
│   └── schema.sql                        // 数据库初始化脚本
└── pom.xml                               // 项目依赖配置
```

## 三、API 设计

API 设计遵循 RESTful 规范，并根据角色（Leader/Worker）进行了物理路径隔离，便于配置不同的安全策略。

| **模块**   | **路径**                             | **方法** | **功能描述**                 |
| ---------- | ------------------------------------ | -------- | ---------------------------- |
| **Leader** | `/api/leader/task`                   | POST     | 创建新任务并指定候选人列表   |
|            | `/api/leader/tasks/{id}/pause`       | POST     | 强制暂停任务                 |
|            | `/api/leader/tasks/{id}/resume`      | POST     | 恢复已暂停的任务             |
|            | `/api/leader/workers`                | GET      | 查看 Worker 列表             |
| **Worker** | `/api/worker/tasks?type=`            | GET      | 查看任务列表                 |
|            | `/api/worker/tasks/{id}/claim`       | POST     | 认领任务（状态转为执行中）   |
|            | `/api/worker/tasks/{id}/finish`      | POST     | 提交并完成任务               |
|            | `/api/worker/tasks/{id}/error-pause` | POST     | 错误暂停任务                 |
| **公共**   | `/api/tasks/{id}`                    | GET      | 查询任务详情（包含状态追踪） |

所有请求需携带 Header：

```
X-User-Id: 1
X-User-Role: LEADER / WORKER
```

## 四、数据库表设计

系统核心表结构如下，特别引入了 `version` 字段用于实现乐观锁。

### 1. 任务主表 (task)

| **字段名**    | **类型**  | **说明**                                 |
| ------------- | --------- | ---------------------------------------- |
| id            | BIGINT    | 主键，自增                               |
| title         | VARCHAR   | 任务标题                                 |
| description   | VARCHAR   | 任务描述                                 |
| status        | VARCHAR   | 枚举：PENDING, IN_PROGRESS, PAUSED, etc. |
| creator_id    | BIGINT    | 任务创建者 ID (Leader)                   |
| assignee_id   | BIGINT    | 当前负责人 ID (Worker)                   |
| error_message | VARCHAR   | 任务错误信息                             |
| created_at    | TIMESTAMP | 任务创建时间                             |
| updated_at    | TIMESTAMP | 任务状态更新时间                         |
| version       | INT       | 乐观锁版本号，防止并发冲突               |

### 2. 任务-候选工作者关联表 (task_candidate_worker)

| **字段名** | **类型** | **说明**       |
| ---------- | -------- | -------------- |
| id         | BIGINT   | 主键，自增     |
| task_id    | BIGINT   | 任务关联 ID    |
| worker_id  | BIGINT   | 候选 Worker ID |

### 3. 用户表 (user)

| **字段名** | **类型**  | **说明**               |
| ---------- | --------- | ---------------------- |
| id         | BIGINT    | 主键，自增             |
| name       | VARCHAR   | 姓名                   |
| role       | VARCHAR   | 角色（LEADER / WORKER) |
| created_at | TIMESTAMP | 创建时间               |
| updated_at | TIMESTAMP | 更新时间               |

## 五、 核心设计思想

本项目在设计上重点解决了四类常见的变更场景，以做到便于修改与扩展

### 1. 切换存储介质

从内存切换到 MySQL，通过配置切换：`storage.type=memory` 或 `storage.type=db`

- **设计模式**：策略模式 + 依赖倒置。
- **实现方案**：定义 `TaskRepository` 接口。通过 Spring 的 `@ConditionalOnProperty` 注解，系统可以根据配置文件中的 `storage.type` 动态决定注入 `MemoryTaskRepositoryImpl` 还是 `DbTaskRepositoryImpl`。
- **扩展效果**：更换数据库时，切换存储方式无需修改业务代码。

### 2. 状态机流转逻辑变更

例如：如果增加“审核中”状态

- **设计模式**：集中式状态机管理。
- **实现方案**：所有的状态检查逻辑都封装在 `TaskServiceImpl` 的特定方法中。
- **扩展效果**：当需要修改状态转换规则时，只需在 Service 层的相应方法中调整 `if` 条件或增加分支，Controller 层完全无感知。

### 3. 并发冲突与异步适配

- **设计模式**：乐观锁机制 (Optimistic Locking)。
- **实现方案**：在更新数据时，SQL 语句带有 `WHERE id = ? AND version = ?` 条件。
- **扩展效果**：当未来系统扩展为高并发架构时，通过 `version` 机制可以天然防止两个 Worker 同时认领同一个任务导致的脏数据问题，无需引入复杂的分布式锁。

### 4. 字段变更

例如：如果增加任务紧急程度

- **实现方案**：使用 Lombok `@Data` 配合统一的接口返回类 `Result<T>`。
- **扩展效果**：增加字段只需修改 `Entity` 类。由于 Controller 返回的是 `Result` 包装对象，前端解析逻辑保持稳定，减少了因接口变动导致的崩溃。

## 六、 运行说明

### 1. 环境要求

- **JDK**: 21
- **Maven**: 3.8+
- **数据库**: MySQL 8.0+

### 2. 启动步骤

1. 克隆代码并进入项目根目录。

2. 配置数据库（默认使用 Memory 模式）：

   - 在 `src/main/resources/application.yml` 中修改 `storage.type: db`。
   - 配置 `spring.datasource` 下的 URL、用户名和密码。

3. 创建数据库：`CREATE DATABASE task_db;`

4. 在数据库中创建表：运行 `schema.sql` 文件以初始化数据库结构。

   - 进入 MySQL， 切换到目标数据库 `task_db`。
   - 运行脚本：`SOURCE .../task-manager/src/main/resources/schema.sql;`

5. 编译并运行：

   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

### 3. 测试请求示例 (Postman)

**场景：Leader 创建任务**

URL: `POST http://localhost:8080/api/leader/task`

Header:

```
X-User-Id: 1
X-User-Role: LEADER
```

Body:（raw-JSON)

```JSON
{
  "title": "测试任务",
  "description": "hello",
  "candidateWorkerIds": [2]
}
```

预期响应：

```json
{
    "code": 0,
    "data": {
        "assigneeId": null,
        "candidateWorkerIds": [
            2
        ],
        "createdAt": "2026-04-26T00:41:38",
        "creatorId": 1,
        "description": "hello",
        "errorMessage": null,
        "id": 1,
        "status": "PENDING",
        "title": "测试任务",
        "updatedAt": "2026-04-26T00:41:38",
        "version": 1
    },
    "message": "success"
}
```

**场景：Worker 认领任务**

URL: `POST http://localhost:8080/api/worker/tasks/1/claim`

Headers:

```
X-User-Id: 2
X-User-Role: WORKER
```

Body:（raw-JSON)

```json
{
  "workerId": "2"
}
```

预期响应:

```json
{
    "code": 0,
    "data": {
        "assigneeId": 2,
        "candidateWorkerIds": [
            2
        ],
        "createdAt": "2026-04-26T00:41:38",
        "creatorId": 1,
        "description": "hello",
        "errorMessage": null,
        "id": 1,
        "status": "IN_PROGRESS",
        "title": "测试任务",
        "updatedAt": "2026-04-26T00:41:38",
        "version": 1
    },
    "message": "success"
}
```
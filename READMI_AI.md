# 指导文档：任务管理系统 (Task Manager)

## 1. 项目背景与目标

本项目是一个基于 Java Spring Boot 的任务管理后端，核心逻辑围绕 **“角色权限隔离”** 与 **“任务全生命周期追踪”** 展开。系统采用了模块化类设计，并严格遵循 **单一职责原则 (SRP)** 和 **开闭原则 (OCP)**。

## 2. 核心架构设计

在协助开发时，请务必遵循以下架构约定：

* **存储解耦 (OCP)**：通过 `TaskRepository` 接口屏蔽底层存储细节。系统支持 `Memory` 和 `Database` 两种实现，通过配置文件 `storage.type` 动态切换。
* **状态机控制**：所有的任务状态流转必须集中在 `TaskServiceImpl` 层。严禁在 Controller 层直接修改任务状态。
* **并发冲突处理**：系统使用 **乐观锁 (Optimistic Locking)** 处理异步状态冲突。`Task` 实体包含 `version` 字段，更新操作必须校验版本号以处理并发覆盖问题。
* **身份校验**：通过 `AuthInterceptor` 拦截器从 Header（`X-User-Id`, `X-User-Role`）中提取用户信息，并通过 `UserContext` (ThreadLocal) 进行全链路共享。

## 3. 业务逻辑约束

在生成或修改逻辑代码时，必须遵守以下业务规则：

### 角色权限 (RBAC)

* **LEADER**：拥有创建任务、全局查询、暂停/恢复任务、查看 Worker 列表的权限。
* **WORKER**：仅能操作分配给自己的任务，拥有认领、完成、错误暂停任务的权限。
* **隔离性**：Leader 和 Worker 角色严格互斥，不可兼任。

### 状态流转图 (State Machine)

请严格按照以下合法路径进行状态变更：

1.  **创建**：无 -> `PENDING` (由 Leader 操作)。
2.  **执行**：`PENDING` -> `IN_PROGRESS` (由 Worker 认领)。
3.  **暂停/恢复**：
    * `PENDING` / `IN_PROGRESS` -> `PAUSED` (由 Leader 强制暂停)。
    * `PAUSED` / `ERROR_PAUSED` -> `PENDING` (由 Leader 恢复)。
4.  **异常/完成**：
    * `IN_PROGRESS` -> `COMPLETED` (由 Worker 完成)。
    * `IN_PROGRESS` -> `ERROR_PAUSED` (由 Worker 上报错误)。

**非法流转处理**：若接收到不符合上述路径的请求，必须抛出 `BusinessException` 并返回错误码 `4003 (STATUS_INVALID)`。

## 4. 技术栈规范

* **基础框架**：Spring Boot, Spring Web, JdbcTemplate。
* **数据模型**：使用 Lombok `@Data` 减少模板代码。
* **响应标准**：统一返回 `com.example.taskmanager.common.Result` 对象。
* **异常处理**：所有业务异常需定义在 `ResultCode` 中，由 `GlobalExceptionHandler` 统一捕获并转为 JSON 格式，严禁暴露堆栈信息。

## 5. AI 操作建议

* **添加新功能**：请先在 `TaskRepository` 定义抽象方法，再分别实现内存版和数据库版，最后在 Service 层编写业务逻辑。
* **修改 API**：请确保在 `LeaderController` 或 `WorkerController` 中增加相应的权限路径校验。
* **Debug**：若出现状态更新失败，请优先检查 `TaskServiceImpl.updateWithLocking` 中的版本校验逻辑。

## 6. Note

`TaskManager.md` 文件包含整体设计要求。在进行修改前请先阅读该文件——其中记录了所有已知设计要求。
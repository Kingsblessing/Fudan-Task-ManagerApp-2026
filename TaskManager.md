# Task Manager 设计要求

使用Java设计⽀持 “⻆⾊权限隔离 + 任务全⽣命周期追踪” 的后端，聚焦模块化类设计与状态流转合理性，⽆需实现⽅法体，重点体现封装原则、SRP 原则与异步场景适配。

## WEEK 1

### 基础业务规则

1. ⻆⾊定义：⽤户⻆⾊分为leader和worker，不可兼任，操作权限严格按⻆⾊划分。

2. leader的设计需要⽅便⽀持以下核⼼功能：

     创建任务：必须指定 “可执⾏该任务的 worker 列表”（1 ⼈）、任务的meta data（例如名称、描述，时间戳），创建后任务默认状态为「待执⾏」；
     任务管控：可将「待执⾏」「执⾏中」状态的任务改为「暂停」；可将「暂停」「错误暂停」状态的任务恢复为「待执⾏」
     任务数据查询：可查看所有任务的完整信息。

     worker数据查询：可查看系统中所有 worker 的 meta data（例如 worker id、当前认领任务情况等）。

3. worker 的设计需要⽅便⽀持以下核⼼功能：
     任务获取：可查询「⾃⼰正在处理的任务」「⾃⼰被指定的任务」「当前可认领的任务」；
     任务认领：仅可认领「待执⾏」状态的任务，认领后任务状态转为「执⾏中」；
     状态更新：可将「执⾏中」的任务改为「完成」或「错误暂停」（需指定错误原因）；
     暂停响应：若 leader 将「执⾏中」的任务改为「暂停」，worker 端需更新状态为「暂停」。

4. 异步状态冲突规则：当同⼀任务同时收到两个来⾃leader和worker冲突操作（如 worker 改「错误暂停」与 leader 改「暂停」），要思考最终状态的合理性与合理的返回值。

### 设计要求

类设计：

- 对系统进⾏分析，识别并划分关键类；
- 对每个类需要明确以下内容：
  - 域的定义语句：清晰描述数据的含义及其⽤途；
  - ⽅法的定义语句：明确⽅法的参数、返回值，以及该⽅法所提供的功能与作⽤。

任务状态定义：定义好任务可能的状态，画出合法的流转图，并讨论不合法的流转如何发现、处置与给出错误（⻅ 异步状态冲突规则）。

## WEEK 2

上面我们设计了⽀持 “⻆⾊权限隔离 + 任务全⽣命周期追踪” 的任务管理 App 后端。

本周需基于上周内容新增任务管理类 TaskSet 来管理任务Task，⽀撑 Leader/Worker 按上周的业务规则对任务进⾏增删查改；请结合继承、多态的知识设计 TaskSet，要求完成：

1. Task 类的设计与实现（完善上面的设计实现）
2. TaskSet 当前基于 Map 存储任务，但需预留扩展为数据库存储的能⼒
3. 通过合理的抽象设计将存储逻辑与 TaskSet 解耦，确保后续切换为数据库存储时，系统满⾜开闭原则（OCP）⸺最⼩化或者不⽤修改 TaskSet、Leader、Worker 的代码
4. 完成你今天设计的完整代码，并基于该设计补全上周仅定义⽅法签名的 Leader、Worker、Task 等相关代码
5. 编写必要的测试代码，联调验证 TaskSet、Leader、Worker 等的核⼼功能是否正常

## WEEK 3

完成可调用的后端实现（任务管理 APP）

### （一）作业背景

前两次作业里你已经把任务管理 App 的核心部分做出来了：角色划分（Leader / Worker）、任务状态流转、Task / Leader / Worker / TaskSet 这些类，以及让 TaskSet 不直接依赖存储实现的抽象设计。

本次作业要求在前两次作业基础上，使用 Spring Boot 将该系统实现为一个可供前端调用的后端服务：

- 使用 Spring Boot 搭建一个简单的 Web 后端项目，设计并实现一组 RESTful 风格接口；
- 将业务逻辑与数据访问逻辑解耦：上次作业中你已经实现了基于内存的 TaskSet，本次作业要求增加“使用数据库完成数据持久化”，并且同时保留内存存储和数据库存储两种实现方式，通过接口抽象来体现解耦设计的优势。系统应支持在启动时通过配置选择使用哪种存储方式（内存或数据库），以体现良好的扩展性与解耦设计。
- 借助大模型，对非法状态流转、参数错误、权限错误等情况给出统一的后端响应；

### （二）功能概述

1. 角色：系统中存在两类角色：LEADER 和 WORKER，二者不可兼任

- LEADER：创建任务、查看全部任务、暂停/恢复任务、查看 Worker 列表及任务情况

- WORKER：查看指派给我/我正在处理/我可认领的任务，认领任务，完成任务，错误暂停任务

- 所有人：查看任务详情

2. 任务状态：统一定义为以下五种：
- PENDING: 待执行
- IN_PROGRESS: 执行中
- PAUSED: 暂停
- ERROR_PAUSED: 错误暂停
- COMPLETED: 已完成

### （三）API 实现约定
1. Base URL：/api
- 请求/响应统一使用 application/json
-  HTTP 状态码统一返回 200，业务成功与否通过响应体中的 code 判断
2. 统一响应结构：
{  "code": 0, "message": "success", "data": {} }
3. 错误码约定：
- 0：成功
- 4001：参数错误
- 4002：任务不存在 
- 4003：任务状态非法
- 4004：任务状态冲突
- 4005：权限不足
- 4006：用户不存在
- 5000：服务器内部错误
4. 身份验证：本次作业不要求实现登录功能，但请你借助大模型，让你的后端按以下约定可以区分当前请求来自 Leader 还是 Worker：
- 使用请求头 X-User-Id 表示当前用户 ID
- 使用请求头 X-User-Role 表示当前用户角色

示例：
```
X-User-Id: 1001
X-User-Role: LEADER
```
或
```
X-User-Id: 2003
X-User-Role: WORKER
```

服务端需要基于该请求头判断当前用户身份，并执行相应权限检查。

### （四）Leader API 定义
1. 创建任务
- POST /api/leader/task
- 请求体：title（string，必填）、description（string，必填）、candidateWorkerIds（array of long，必填，至少 1 人）
- 响应 data：{ taskId, status: "PENDING" }
2. 查看全部任务
- GET /api/leader/tasks
- 可选查询参数：status、keyword（标题/描述模糊查询）、workerId（按认领人筛选）
- 响应 data：任务列表，每项包含 id、title、description、status、creatorId、assigneeId、candidateWorkerIds、errorMessage、createdAt、updatedAt
3. 暂停任务
- POST /api/leader/tasks/{taskId}/pause
- 请求体（可选）：reason
- 响应 data：{ taskId, status: "PAUSED" }
4. 恢复任务
- POST /api/leader/tasks/{taskId}/resume
- 响应 data：{ taskId, status: "PENDING" }
5. 查看 Worker 列表
- GET /api/leader/workers
- 响应 data：Worker 列表，每项至少包含 workerId、workerName、被指派任务数、正在处理任务数、已完成任务数
### （五）Worker API 定义
6. 查看任务列表
- GET /api/worker/tasks?type=
- type 参数（必填）：
  - assigned：我在候选列表中的任务
  - processing：当前由我认领并处理中的任务
  - claimable：我可认领且状态为 PENDING 的任务
- 响应 data：任务列表，每项包含 id、title、description、status、assigneeId、candidateWorkerIds、createdAt、updatedAt
7. 认领任务
- POST /api/worker/tasks/{taskId}/claim
- 约束：任务状态为 PENDING，当前 Worker 在候选列表中，且任务尚未被他人认领
- 响应 data：{ taskId, status: "IN_PROGRESS", assigneeId }
8. 完成任务
- POST /api/worker/tasks/{taskId}/finish
- 约束：任务状态为 IN_PROGRESS，且认领人是自己
- 响应 data：{ taskId, status: "COMPLETED" }
9. 错误暂停任务
- POST /api/worker/tasks/{taskId}/error-pause
- 请求体：errorMessage（string，必填）
- 约束：任务状态为 IN_PROGRESS，且认领人是自己
- 响应 data：{ taskId, status: "ERROR_PAUSED", errorMessage }
### （六）公共 API 定义
10. 查看任务详情
- GET /api/tasks/{taskId}
- 权限：Leader 可查看任意任务；Worker 至少可查看自己在候选列表中或已认领的任务（可自行设计更严格或宽松的规则，但需在文档中说明）
- 响应 data：任务完整信息，包含 id、title、description、status、creatorId、assigneeId、candidateWorkerIds、errorMessage、createdAt、updatedAt
### （七）数据库设计
- user 表：id、name、role（LEADER/WORKER）、created_at、updated_at
- task 表：id、title、description、status、creator_id、assignee_id、error_message、created_at、updated_at、version（可选）
- task_candidate_worker 表：id、task_id、worker_id
### （八）代码要求
1. 技术栈用 Java + Spring Boot，IDE 是 VS Code, 数据访问方案可自选（用JdbcTemplate），数据库使用 MySQL。
2. 项目至少应当包含以下结构：
src/main/java/.../
├── controller // 接收请求，参数校验，返回响应
├── service // 业务逻辑接口
│ └── impl // 业务逻辑实现
├── repository // 数据访问层（DAO）
├── entity // 数据库实体类（对应表）
├── exception // 自定义异常类
└── common // 通用工具类、统一返回格式等
Controller 层只做请求转发与响应封装，状态流转逻辑集中在 Service 层，存储细节封装在 Repository 层，切换数据库时不应影响上层业务代码。
3. 系统需通过统一异常处理机制，将参数异常、业务异常（任务不存在、状态非法、权限不足、任务已被认领）、系统异常统一转换为 JSON 响应，不得将异常栈暴露给前端。 
### （九）API 测试
测试覆盖：创建/查询/认领/完成任务、错误暂停、Leader 暂停与恢复、非法状态流转、权限错误。
测试形式不限（JUnit、Spring Boot 接口测试、Postman/Apifox 均可），测试代码不要求提交。
# 任务管理系统 (Task-Manager) 项目文档

## 一、项目概述

本项目实现了一个支持"角色权限隔离（Leader / Worker）+ 任务全生命周期管理"的全栈系统。

系统支持任务创建、认领、执行、暂停、恢复及错误处理，并提供两种存储方式：

- 内存存储（快速开发 / 测试）
- 数据库存储（MySQL + JdbcTemplate）

设计重点在于：高内聚、低耦合、易扩展；设计满足单一职责原则 SRP、开闭原则 OCP。

**后端技术栈**：Java 21, Spring Boot 4.0.5, Spring JDBC, MySQL, Lombok, JWT (jjwt), BCrypt, SLF4J + Logback

**前端技术栈**：Vue 3, Vite, Vue Router 4, Axios, Element Plus

## 二、项目结构

### 2.1 后端结构

项目后端采用分层架构设计，严格遵循单一职责原则 SRP，确保各层级逻辑清晰：

```
task-manager
├── src/main/java/com/example/taskmanager
│   ├── TaskManagerApplication.java       // 应用启动类
│   ├── common                            // 通用组件
│   │   ├── AuthInterceptor.java          // JWT 鉴权拦截器（Cookie 验证 + 黑名单 + RBAC）
│   │   ├── JwtUtil.java                  // JWT 工具类（双 Token 生成/解析/校验）
│   │   ├── TokenBlacklist.java           // Token 黑名单服务（内存/DB 双实现）
│   │   ├── Result.java                   // 统一响应格式
│   │   ├── ResultCode.java               // 错误码字典
│   │   └── UserContext.java              // 当前用户上下文（基于ThreadLocal）
│   ├── config
│   │   ├── WebConfig.java                // 注册拦截器
│   │   ├── StorageProperties.java        // 配置类
│   │   ├── CorsConfig.java               // CORS 跨域配置
│   │   └── DataInitializer.java          // 启动时密码初始化
│   ├── controller                        // 控制器层
│   │   ├── AuthController.java           // 认证控制器（Token 刷新、登出）
│   │   ├── LoginController.java          // 登录接口（密码校验 + JWT 签发）
│   │   ├── CommonController.java         // 通用接口控制器
│   │   ├── LeaderController.java         // 管理员（负责人）接口控制器
│   │   └── WorkerController.java         // 普通工作者接口控制器
│   ├── entity                            // 领域实体
│   │   ├── Role.java                     // 角色枚举
│   │   ├── Task.java                     // 任务实体
│   │   ├── TaskStatus.java               // 任务状态枚举
│   │   └── User.java                     // 用户实体（含 password 字段）
│   ├── exception                         // 异常处理
│   │   ├── BusinessException.java        // 自定义业务异常
│   │   └── GlobalExceptionHandler.java   // 全局异常处理器（含日志记录）
│   ├── repository                        // 仓储层（数据访问）
│   │   ├── TaskRepository.java           // 任务存储抽象接口
│   │   ├── impl
│   │   │   ├── DbTaskRepositoryImpl.java // MySQL 实现
│   │   │   ├── MemoryTaskRepositoryImpl.java // HashMap 内存实现
│   │   │   ├── DbUserRepositoryImpl.java // 用户 MySQL 实现
│   │   │   └── MemoryUserRepositoryImpl.java // 用户内存实现（BCrypt 密码）
│   │   └── UserRepository.java           // 用户仓储接口
│   ├── service                           // 服务层（业务逻辑）
│   │   ├── TaskService.java              // 任务服务接口
│   │   ├── UserService.java              // 用户认证服务接口
│   │   └── impl
│   │       ├── TaskServiceImpl.java      // 核心状态机与业务逻辑（含日志）
│   │       └── UserServiceImpl.java      // 用户认证服务（BCrypt 密码校验）
│   └── aop                               // 自定义 AOP 框架
│       ├── annotation/DebugLog.java      // Debug 日志注解
│       └── core/                         // AOP 代理工厂
├── src/main/resources
│   ├── application.yml                   // 应用配置（存储切换 + JWT 密钥）
│   ├── schema.sql                        // 数据库初始化脚本（含 password + token_blacklist）
│   └── logback-spring.xml                // 日志配置（控制台 + 文件 + JSON + Kibana）
└── pom.xml                               // 项目依赖配置
```

### 2.2 原版前端结构

```
frontend/
├── index.html
├── vite.config.js           // Vite 配置（含 /api 代理）
├── package.json
└── src/
    ├── main.js              // 入口：挂载 ElementPlus + Router
    ├── App.vue              // 根组件，仅 <router-view />
    ├── style.css            // 全局样式（极简）
    ├── api/
    │   └── index.js         // Axios 实例 + 请求/响应拦截器 + Token 刷新逻辑
    ├── router/
    │   └── index.js         // 路由配置 + beforeEnter 守卫 + 登出逻辑
    ├── utils/
    │   ├── crypto.js        // AES-256-GCM 加密/解密工具
    │   └── storage.js       // 加密 localStorage 封装
    └── pages/
        ├── LoginPage.vue    // 登录页（含密码输入）
        ├── LeaderPage.vue   // Leader 管理面板（任务列表 + Worker 列表）
        ├── WorkerPage.vue   // Worker 工作台（可认领/进行中/我的任务）
        └── TaskDetailPage.vue // 任务详情页
```

### 2.3 新版前端架构

```
newfrontend/
├── index.html                 # 入口 HTML
├── package.json               # 依赖配置
├── vite.config.js             # Vite 配置（含 API 代理）
└── src/
    ├── main.js                # 应用入口
    ├── App.vue                # 根组件（含主题/视图切换器）
    ├── api/
    │   └── index.js           # Axios 实例 + API 函数 + Token 刷新
    ├── router/
    │   └── index.js           # 路由配置 + 鉴权守卫 + 登出逻辑
    ├── composables/
    │   ├── useTheme.js        # 主题 + 视图模式管理
    │   ├── useToast.js        # Toast 通知系统
    │   └── useStatus.js       # 任务状态工具函数
    ├── components/
    │   └── ToastContainer.vue # Toast 容器组件
    ├── utils/
    │   ├── crypto.js          // AES-256-GCM 加密/解密工具
    │   └── storage.js         // 加密 localStorage 封装
    ├── pages/
    │   ├── LoginPage.vue      # 登录页（含密码输入）
    │   ├── LeaderPage.vue     # Leader 管理面板
    │   ├── WorkerPage.vue     # Worker 工作台
    │   └── TaskDetailPage.vue # 任务详情页
    └── assets/
        └── styles/
            └── main.css       # 全局样式 + CSS 变量
```

## 三、安全功能

### 3.1 认证与鉴权架构

系统采用 **JWT 双 Token + httpOnly Cookie** 方案，替代了早期的 Header 伪鉴权：

```
┌──────────┐     POST /api/login       ┌──────────┐
│ Frontend │ ───────────────────────>  │ Backend  │
│  (Vue)   │ <──── Set-Cookie ───────  │ (Spring) │
│          │   access_token (httpOnly) │          │
│          │   refresh_token (httpOnly)│          │
└──────────┘                           └──────────┘
      │                                      │
      │  每次请求自动携带 Cookie              │
      │ ───────────────────────────────────> │
      │                                      │ AuthInterceptor:
      │                                      │  1. 从 Cookie 提取 JWT
      │                                      │  2. 验证签名 + 过期时间
      │                                      │  3. 检查 Token 黑名单
      │                                      │  4. 提取 userId/role → UserContext
      │                                      │  5. RBAC 路径权限校验
```

### 3.2 密码安全

- **BCrypt 哈希**：用户密码使用 `BCryptPasswordEncoder` 哈希存储，内置随机盐值
- **统一错误提示**：登录失败时统一返回"用户名或密码错误"，不暴露具体原因
- **启动初始化**：`DataInitializer` 在应用启动时自动确保数据库密码为正确的 BCrypt 哈希

### 3.3 JWT 双 Token 机制

| Token 类型 | 有效期 | 用途 |
|------------|--------|------|
| **Access Token** | 15 分钟 | 请求鉴权，短期有效降低泄露风险 |
| **Refresh Token** | 7 天 | 静默刷新 Access Token，仅用于 `/api/refresh` |

- **签名算法**：HS256
- **传输方式**：httpOnly Cookie（JS 不可读，防止 XSS 窃取）
- **前端静默刷新**：Access Token 过期时自动调用 `/api/refresh`，用户无感知

### 3.4 Token 黑名单（登出注销）

- 用户登出时将 Access + Refresh Token 的 jti 加入服务端黑名单
- `TokenBlacklist` 提供两种实现：内存模式（ConcurrentHashMap）和数据库模式（token_blacklist 表）
- 鉴权时 `AuthInterceptor` 先检查 Token 是否在黑名单中

### 3.5 前端数据加密

- **Token 存储**：通过 httpOnly Cookie 传输，前端 JS 无法读取
- **localStorage 加密**：用户展示信息使用 AES-256-GCM 加密存储，密钥由 userId 派生
- **篡改检测**：解密失败时自动清除数据并跳转登录页

### 3.6 测试账号

所有测试账号统一密码：`password123`

| 用户 ID | 角色   | 姓名        |
| ------- | ------ | ----------- |
| 1001    | LEADER | Leader-张三 |
| 1002    | LEADER | Leader-李四 |
| 2001    | WORKER | Worker-王五 |
| 2002    | WORKER | Worker-赵六 |
| 2003    | WORKER | Worker-钱七 |
| 2004    | WORKER | Worker-孙八 |
| 2005    | WORKER | Worker-周九 |

## 四、日志功能

### 4.1 日志架构

系统使用 **SLF4J + Logback** 日志框架，支持结构化 JSON 输出，便于 Kibana 采集分析。

```
┌─────────────────────┐    ┌───────────────────────┐    ┌─────────────────────┐    ┌─────────────┐
│ Application Service │    │       Filebeat        │    │   Elasticsearch     │    │   Kibana    │
│      Logback        │───>│ Lightweight Collector │───>│ Storage & Indexing  │───>│Visualization│
│     JSON Logs       │    │ Listens to log files  │    │  Full-text search   │    │  Dashboards │
└─────────────────────┘    └───────────────────────┘    └─────────────────────┘    └─────────────┘
```

### 4.2 日志输出配置

| Appender | 格式 | 级别 | 文件路径 |
|----------|------|------|----------|
| **CONSOLE** | 带颜色文本 | INFO | 控制台 |
| **FILE** | JSON (LogstashEncoder) | DEBUG | `logs/task-manager.log` |
| **ERROR_FILE** | JSON (LogstashEncoder) | ERROR | `logs/task-manager-error.log` |
| **KIBANA_FILE** | JSON (LogstashEncoder) | INFO | `logs/task-manager-kibana.json` |

### 4.3 日志覆盖层次

| 层次 | 日志内容 | 级别 |
|------|----------|------|
| **Controller** | 请求入口参数、操作类型 | INFO / DEBUG |
| **Service** | 业务状态变更、乐观锁冲突、权限拒绝 | INFO / WARN |
| **Auth** | 鉴权拦截、Token 失效、RBAC 拒绝、登出 | DEBUG / WARN |
| **Exception** | 业务异常、系统异常（含堆栈） | WARN / ERROR |

### 4.4 Kibana 可视化

部署 ELK 栈后可通过 Kibana 实现：

- **错误趋势**：按 `level:ERROR` 聚合的时间轴折线图
- **用户行为审计**：按 `userId` + `uri` 聚合的操作频次
- **安全事件**：筛选 `鉴权失败`、`权限拒绝` 等关键词
- **任务状态变更**：按时间排列的状态变更记录

详细部署方式见 `docker-compose.yml` 和 `filebeat.yml`。

## 五、API 设计

### 5.1 认证接口

| 路径 | 方法 | 说明 | 认证要求 |
|------|------|------|----------|
| `/api/login` | POST | 用户登录（密码校验 + JWT 签发） | 无 |
| `/api/refresh` | POST | 刷新 Access Token | Refresh Token Cookie |
| `/api/logout` | POST | 用户登出（Token 加入黑名单） | Access Token Cookie |

### 5.2 业务接口

| **模块**   | **路径**                             | **方法** | **功能描述**                 |
| ---------- | ------------------------------------ | -------- | ---------------------------- |
| **Leader** | `/api/leader/task`                   | POST     | 创建新任务并指定候选人列表   |
|            | `/api/leader/tasks`                  | GET      | 查看全部任务（支持筛选）     |
|            | `/api/leader/tasks/{id}/pause`       | POST     | 强制暂停任务                 |
|            | `/api/leader/tasks/{id}/resume`      | POST     | 恢复已暂停的任务             |
|            | `/api/leader/workers`                | GET      | 查看 Worker 列表             |
| **Worker** | `/api/worker/tasks?type=`            | GET      | 查看任务列表                 |
|            | `/api/worker/tasks/{id}/claim`       | POST     | 认领任务（状态转为执行中）   |
|            | `/api/worker/tasks/{id}/finish`      | POST     | 提交并完成任务               |
|            | `/api/worker/tasks/{id}/error-pause` | POST     | 错误暂停任务                 |
| **公共**   | `/api/tasks/{id}`                    | GET      | 查询任务详情（包含状态追踪） |

所有业务请求需通过 httpOnly Cookie 携带 JWT Token，后端 `AuthInterceptor` 自动验证。

## 六、数据库表设计

### 6.1 任务主表 (task)

| **字段名**    | **类型**  | **说明**                                 |
| ------------- | --------- | ---------------------------------------- |
| id            | BIGINT    | 主键，自增                               |
| title         | VARCHAR   | 任务标题                                 |
| description   | VARCHAR   | 任务描述                                 |
| status        | VARCHAR   | 枚举：PENDING, IN_PROGRESS, PAUSED, etc. |
| creator_id    | BIGINT    | 任务创建者 ID (Leader)                   |
| assignee_id   | BIGINT    | 当前负责人 ID (Worker)                   |
| error_message | VARCHAR   | 任务错误信息                             |
| version       | INT       | 乐观锁版本号，防止并发冲突               |
| created_at    | TIMESTAMP | 任务创建时间                             |
| updated_at    | TIMESTAMP | 任务状态更新时间                         |

### 6.2 用户表 (user)

| **字段名** | **类型**  | **说明**               |
| ---------- | --------- | ---------------------- |
| id         | BIGINT    | 主键，自增             |
| name       | VARCHAR   | 姓名                   |
| role       | VARCHAR   | 角色（LEADER / WORKER) |
| password   | VARCHAR   | BCrypt 哈希密码        |
| created_at | TIMESTAMP | 创建时间               |
| updated_at | TIMESTAMP | 更新时间               |

### 6.3 任务-候选工作者关联表 (task_candidate_worker)

| **字段名** | **类型** | **说明**       |
| ---------- | -------- | -------------- |
| id         | BIGINT   | 主键，自增     |
| task_id    | BIGINT   | 任务关联 ID    |
| worker_id  | BIGINT   | 候选 Worker ID |

### 6.4 Token 黑名单表 (token_blacklist)

| **字段名** | **类型**  | **说明**                    |
| ---------- | --------- | --------------------------- |
| id         | BIGINT    | 主键，自增                  |
| jti        | VARCHAR   | JWT ID（唯一索引）          |
| expires_at | TIMESTAMP | Token 过期时间（用于清理）  |
| created_at | TIMESTAMP | 记录创建时间                |

## 七、任务状态机

```
(null) → PENDING              [Leader 创建任务]
PENDING → IN_PROGRESS         [Worker 认领任务]
PENDING/IN_PROGRESS → PAUSED  [Leader 暂停任务]
PAUSED/ERROR_PAUSED → PENDING [Leader 恢复任务]
IN_PROGRESS → COMPLETED       [Worker 完成任务]
IN_PROGRESS → ERROR_PAUSED    [Worker 错误暂停任务]
```

状态流转集中在 `TaskServiceImpl` 中处理，非法流转抛出 `BusinessException`（错误码 4003）。

## 八、前后端连接方式

### 8.1 跨域解决方案

采用 **Vite 开发代理** + 后端 **CorsConfig** 双重保障：

```js
// vite.config.js
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
}
```

### 8.2 Axios 拦截器

```js
// 请求：自动携带 httpOnly Cookie（Token 由后端通过 Set-Cookie 下发）
const api = axios.create({ withCredentials: true })

// 响应：统一处理业务错误 + Token 静默刷新
api.interceptors.response.use(
  (res) => { /* 成功处理 */ },
  async (err) => {
    if (err.response?.data?.code === 4005) {
      // Token 失效 → 调用 /api/refresh 静默刷新 → 重试原请求
    }
  }
)
```

### 8.3 登录流程

1. 前端提交 `{ userId, password, role }` 到 `POST /api/login`
2. 后端校验密码（BCrypt）和角色，签发 Access + Refresh Token
3. Token 通过 `Set-Cookie: httpOnly; Secure; SameSite=Strict` 下发
4. 前端仅存储用户展示信息（userId、role、name）到 localStorage
5. 后续请求浏览器自动携带 Cookie，`AuthInterceptor` 验证 JWT

### 8.4 登出流程

1. 前端调用 `POST /api/logout`
2. 后端将当前 Access + Refresh Token 加入黑名单
3. 后端清除 Cookie（`Set-Cookie: Max-Age=0`）
4. 前端清除 localStorage，跳转登录页

## 九、页面与交互设计

### 9.1 登录页面 (`/login`)

- 用户 ID 输入 + 密码输入 + 角色选择
- 测试账号快速登录（自动填入 ID、密码、角色）
- 响应式布局

### 9.2 Leader 页面 (`/leader`)

- 任务列表（表格/卡片视图切换）
- 任务筛选（状态、关键词）
- 创建任务（标题、描述、候选 Worker 多选）
- 暂停/恢复任务
- Worker 列表查看

### 9.3 Worker 页面 (`/worker`)

- 三个 Tab：可认领任务、进行中任务、我的任务
- 认领、完成、错误暂停操作
- 错误暂停需填写错误原因

### 9.4 任务详情页 (`/task/:id`)

- 完整任务信息展示
- 状态标签、时间格式化、错误信息高亮
- Leader 可查看任意任务，Worker 仅可查看自己相关的任务

## 十、运行说明

### 10.1 环境要求

| 依赖    | 版本要求                       |
| ------- | ------------------------------ |
| JDK     | 21+                            |
| Node.js | 18+                            |
| npm     | 9+                             |
| MySQL   | 8.0+（仅使用数据库存储时需要） |
| Docker  | 可选（部署 ELK 日志栈时需要）  |

### 10.2 启动步骤

**1. 克隆代码并进入项目根目录。**

**2. 配置数据库（默认使用 Memory 模式）：**

- 在 `src/main/resources/application.yml` 中修改 `storage.type: db`
- 配置 `spring.datasource` 下的 URL、用户名和密码
- 创建数据库：`CREATE DATABASE task_db;`
- 在数据库中运行 `schema.sql` 初始化表结构

**3. 启动后端**

```bash
cd task-manager

# 内存存储模式（无需数据库）
./mvnw spring-boot:run

# 数据库存储模式（需先配置 MySQL）
# 修改 application.yml: storage.type: db
./mvnw spring-boot:run
```

后端启动后监听 `http://localhost:8080`。

**4. 启动前端**

```bash
# 旧版前端（Element Plus）
cd frontend && npm install && npm run dev
# 访问 http://localhost:5173

# 新版前端（自定义 UI）
cd newfrontend && npm install && npm run dev
# 访问 http://localhost:5174
```

**5. 启动日志可视化（可选）**

```bash
# 启动 ELK 栈（Elasticsearch + Kibana + Filebeat）
docker-compose up -d

# 访问 Kibana：http://localhost:5601
# 创建 Index Pattern: task-manager-logs-*
```

### 10.3 测试账号

所有账号密码统一为 `password123`。

| 用户 ID | 角色   | 姓名        |
| ------- | ------ | ----------- |
| 1001    | LEADER | Leader-张三 |
| 1002    | LEADER | Leader-李四 |
| 2001    | WORKER | Worker-王五 |
| 2002    | WORKER | Worker-赵六 |
| 2003    | WORKER | Worker-钱七 |
| 2004    | WORKER | Worker-孙八 |
| 2005    | WORKER | Worker-周九 |

## 十一、依赖说明

### 后端核心依赖

| 依赖 | 用途 |
|------|------|
| `spring-boot-starter-webmvc` | Web 框架 |
| `spring-boot-starter-data-jpa` | 数据访问（JdbcTemplate） |
| `mysql-connector-j` | MySQL 驱动 |
| `lombok` | 实体类简化 |
| `jjwt-api/impl/jackson` | JWT Token 生成与解析 |
| `spring-security-crypto` | BCrypt 密码编码器 |
| `logstash-logback-encoder` | JSON 结构化日志输出 |

### 前端核心依赖

| 依赖 | 用途 |
|------|------|
| `vue` + `vue-router` | 前端框架 |
| `axios` | HTTP 请求 |
| `element-plus` | UI 组件库（旧版前端） |

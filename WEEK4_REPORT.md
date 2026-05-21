# WEEK 4 前端实现报告

## 一、技术栈

| 类别 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 框架 | Vue 3 | 3.5 | Composition API (`<script setup>`) |
| 构建工具 | Vite | 8.x | 快速热重载、内置代理解决跨域 |
| 路由 | Vue Router 4 | 4.x | 路由守卫实现角色鉴权 |
| HTTP 请求 | Axios | 1.x | 拦截器统一注入请求头与错误处理 |
| UI 组件库 | Element Plus | 2.x | 提供表格、表单、对话框等现成组件 |

选择 Vue 3 + Element Plus 的理由：Vue 3 的 Composition API 代码组织清晰，Element Plus 开箱即用，适合快速搭建管理类页面。

## 二、项目结构

```
frontend/
├── index.html
├── vite.config.js          # Vite 配置（含 /api 代理）
├── package.json
└── src/
    ├── main.js              # 入口：挂载 ElementPlus + Router
    ├── App.vue              # 根组件，仅 <router-view />
    ├── style.css            # 全局样式（极简）
    ├── api/
    │   └── index.js         # Axios 实例 + 请求/响应拦截器 + 全部 API 函数
    ├── router/
    │   └── index.js         # 路由配置 + beforeEnter 守卫
    └── pages/
        ├── LoginPage.vue    # 登录页
        ├── LeaderPage.vue   # Leader 管理面板（任务列表 + Worker 列表）
        ├── WorkerPage.vue   # Worker 工作台（可认领/进行中/我的任务）
        └── TaskDetailPage.vue # 任务详情页
```

## 三、前后端连接方式

### 3.1 跨域解决方案

采用 **Vite 开发代理**（推荐方案）：

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

前端请求 `/api/...` 时，Vite 开发服务器会自动转发到后端 `localhost:8080`，浏览器端无跨域问题。同时后端也配置了 `CorsConfig`，直接访问同样可用。

### 3.2 Axios 拦截器

```js
// 请求拦截：自动注入身份头
api.interceptors.request.use((config) => {
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  if (user) {
    config.headers['X-User-Id'] = user.userId
    config.headers['X-User-Role'] = user.role
  }
  return config
})

// 响应拦截：统一处理业务错误码
api.interceptors.response.use((res) => {
  if (res.data.code !== 0) {
    ElMessage.error(res.data.message)  // 自动弹出错误提示
    return Promise.reject(res.data)
  }
  return res.data
})
```

所有页面调用 API 时无需重复处理错误，拦截器统一展示后端返回的错误信息。

### 3.3 登录与身份持久化

- 前端通过 `POST /api/login` 携带 `{ userId, role }` 调用后端验证
- 后端检查用户是否存在、角色是否匹配
- 登录成功后将 `{ userId, role }` 存入 `localStorage`
- 页面刷新后 Axios 拦截器从 `localStorage` 读取身份，自动附带到每次请求
- 退出登录时清除 `localStorage` 并跳转回登录页

## 四、页面与交互设计

### 4.1 登录页面 (`/login`)

**布局**：居中卡片式表单，渐变背景。

**控件**：
- 用户 ID 数字输入框（范围 1001-9999）
- 角色单选按钮组（LEADER / WORKER）
- 登录按钮

**交互细节**：
- 页面底部提供**测试账号标签**，点击即可快速填入 ID 和角色
- 登录失败时，响应拦截器自动弹出错误提示（如"用户角色不匹配"）
- 登录成功后根据角色自动跳转到 `/leader` 或 `/worker`

### 4.2 Leader 页面 (`/leader`)

**布局**：顶栏 + 左侧菜单 + 右侧内容区（经典后台管理布局）。

**左侧菜单**：两个 Tab 切换
- **任务列表**：展示所有任务表格
- **Worker 列表**：展示所有 Worker 统计信息

**任务列表功能**：
- 表格列：ID、标题、描述、状态（彩色标签）、执行人
- **筛选栏**：状态下拉 + 关键词输入 + 搜索按钮
- **操作列**：
  - PENDING/IN_PROGRESS 状态显示「暂停」按钮
  - PAUSED/ERROR_PAUSED 状态显示「恢复」按钮
- 点击行任意位置跳转到任务详情页
- 空列表显示"暂无任务"占位

**新建任务**：
- 点击「+ 新建任务」弹出对话框
- 填写标题（文本输入）、描述（多行文本）、候选 Worker（多选下拉，选项来自 Worker 列表）
- 创建成功后自动刷新任务列表

**Worker 列表**：
- 表格列：Worker ID、姓名、被指派任务数、进行中任务数、已完成任务数

### 4.3 Worker 页面 (`/worker`)

**布局**：与 Leader 一致（顶栏 + 左侧菜单 + 内容区），顶栏颜色区分角色。

**左侧菜单**：三个 Tab
- **可认领任务** (`claimable`)：状态为 PENDING 且自己在候选列表中
- **进行中任务** (`processing`)：当前由自己认领且执行中的任务
- **我的任务** (`assigned`)：所有自己被指派的任务

**操作按钮**（根据 Tab 动态显示）：
- 可认领列表：「认领」按钮
- 进行中列表：「完成」按钮 + 「错误暂停」按钮

**错误暂停交互**：
- 点击「错误暂停」弹出对话框
- 必须填写错误原因（多行文本）
- 提交后调用 `POST /api/worker/tasks/{id}/error-pause`

### 4.4 任务详情页 (`/task/:id`)

**布局**：顶栏（含返回按钮）+ 居中卡片。

**展示内容**：
- 使用 Element Plus 的 `Descriptions` 组件展示所有字段
- 任务 ID、状态（彩色标签）、标题、描述
- 创建人、执行人（未认领显示"未认领"）
- 候选 Worker 列表（标签形式）
- 错误信息（红色高亮）
- 创建时间、更新时间（本地化格式）

**角色差异**：
- Leader 可查看任意任务
- Worker 只能查看自己在候选列表中或已认领的任务（后端权限校验）

## 五、路由与鉴权

```js
routes: [
  { path: '/', redirect: '/login' },
  { path: '/login' },
  { path: '/leader',  meta: { requiresAuth: true, role: 'LEADER' } },
  { path: '/worker',  meta: { requiresAuth: true, role: 'WORKER' } },
  { path: '/task/:id', meta: { requiresAuth: true } },
]
```

路由守卫逻辑：
1. 无需认证的页面直接放行
2. 需要认证但未登录 → 跳转 `/login`
3. 已登录但角色不匹配 → 跳转到对应角色的首页

## 六、运行说明

### 6.1 环境要求

| 依赖 | 版本要求 |
|------|----------|
| JDK | 21+ |
| Node.js | 18+ |
| npm | 9+ |
| MySQL（可选） | 8.0+（仅使用数据库存储时需要） |

### 6.2 启动步骤

**1. 启动后端**

```bash
cd task-manager

# 方式一：Maven 直接启动（内存存储，无需数据库）
./mvnw spring-boot:run

# 方式二：如需数据库存储，先修改 src/main/resources/application.yml
#   storage.type: db
# 确保 MySQL 运行在 localhost:3306，数据库 task_db 已创建
# 然后执行 ./mvnw spring-boot:run
```

后端启动后监听 `http://localhost:8080`。

**2. 启动前端**

```bash
cd task-manager/frontend

# 安装依赖（首次）
npm install

# 启动开发服务器
npm run dev
```

前端启动后访问 `http://localhost:5173`。

### 6.3 测试账号

| 用户 ID | 角色 | 姓名 |
|---------|------|------|
| 1001 | LEADER | Leader-张三 |
| 1002 | LEADER | Leader-李四 |
| 2001 | WORKER | Worker-王五 |
| 2002 | WORKER | Worker-赵六 |
| 2003 | WORKER | Worker-钱七 |
| 2004 | WORKER | Worker-孙八 |
| 2005 | WORKER | Worker-周九 |

登录页面底部的测试账号标签支持一键填入。

### 6.4 测试流程建议

1. 用 `1001 (LEADER)` 登录 → 进入 Leader 管理面板
2. 新建任务，选择 1-2 个候选 Worker → 任务出现在列表中，状态为「待执行」
3. 点击任务 → 进入详情页，确认信息正确
4. 返回，暂停任务 → 状态变为「暂停」
5. 恢复任务 → 状态恢复为「待执行」
6. 切换登录为 `2001 (WORKER)` → 进入 Worker 工作台
7. 在「可认领任务」中认领任务 → 状态变为「执行中」
8. 在「进行中任务」中完成任务 → 状态变为「已完成」
9. 再认领一个新任务，使用「错误暂停」→ 填写错误原因，状态变为「错误暂停」
10. 操作失败场景：用 Worker 尝试暂停 Leader 专属操作 → 页面弹出错误提示

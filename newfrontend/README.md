# Task Manager — New Frontend

全新的任务管理系统前端，采用双模式设计系统。

## 设计理念

### 主题模式

系统会自动检测设备的主题偏好，并支持手动切换：

- **自动模式**（默认）— 跟随系统主题设置
- **浅色模式** — 白色背景 + 蓝色强调色 (#5c7cfa)
- **深色模式** — 深色背景 (#0a0a12) + 紫色强调色 (#845ef7)

### 视图模式

独立于主题，可自由切换：

- **列表视图** — 表格展示数据，适合快速浏览和对比
- **卡片视图** — 卡片展示数据，适合沉浸式浏览

## 技术栈

- **Vue 3** — Composition API + `<script setup>`
- **Vite** — 极速构建工具
- **Tailwind CSS** — 实用优先的 CSS 框架
- **Axios** — HTTP 客户端
- **Vue Router** — 路由管理

## 快速开始

```bash
# 安装依赖
npm install

# 启动开发服务器（端口 5174）
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview
```

## 项目结构

```
newfrontend/
├── index.html                 # 入口 HTML
├── package.json               # 依赖配置
├── vite.config.js             # Vite 配置（含 API 代理）
├── tailwind.config.js         # Tailwind 配置（自定义主题）
├── postcss.config.js          # PostCSS 配置
└── src/
    ├── main.js                # 应用入口
    ├── App.vue                # 根组件（含主题/视图切换器）
    ├── api/
    │   └── index.js           # Axios 实例 + API 函数
    ├── router/
    │   └── index.js           # 路由配置 + 鉴权守卫
    ├── composables/
    │   ├── useTheme.js        # 主题 + 视图模式管理
    │   ├── useToast.js        # Toast 通知系统
    │   └── useStatus.js       # 任务状态工具函数
    ├── components/
    │   └── ToastContainer.vue # Toast 容器组件
    ├── pages/
    │   ├── LoginPage.vue      # 登录页
    │   ├── LeaderPage.vue     # Leader 管理面板
    │   ├── WorkerPage.vue     # Worker 工作台
    │   └── TaskDetailPage.vue # 任务详情页
    └── assets/
        └── styles/
            └── main.css       # 全局样式 + CSS 变量
```

## 控制按钮

右下角有两个浮动按钮：

### 主题切换

点击循环切换：自动 → 浅色 → 深色

- **自动** — 跟随系统主题，显示当前实际应用的主题
- **浅色** — 强制浅色模式
- **深色** — 强制深色模式

### 视图切换

点击在列表/卡片视图间切换：

- **列表** — 表格形式展示数据
- **卡片** — 卡片形式展示数据

## 功能特性

### 登录页

- 用户 ID 输入 + 角色选择
- 测试账号快速登录
- 响应式布局，移动端友好

### Leader 管理面板

- 任务列表（列表/卡片视图）
- 任务筛选（状态、关键词）
- 创建任务（标题、描述、候选 Worker）
- 暂停/恢复任务
- Worker 列表查看

### Worker 工作台

- 可认领任务列表
- 进行中任务列表
- 我的任务列表
- 认领、完成、错误暂停操作

### 任务详情页

- 完整任务信息展示
- 状态标签、时间格式化
- 错误信息高亮显示

## 响应式设计

所有页面均支持：

- **桌面端** — 完整侧边栏 + 内容区布局
- **平板端** — 自适应网格布局
- **移动端** — 单列布局，侧边栏隐藏

## 后端连接

默认代理到 `http://localhost:8080`，需要后端服务运行。

```bash
# 在 task-manager 根目录启动后端
./mvnw spring-boot:run
```

## 测试账号

| 用户 ID | 角色 |
|---------|------|
| 1001 | LEADER |
| 1002 | LEADER |
| 2001-2005 | WORKER |

-- 任务主表
CREATE TABLE `task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,        -- 任务ID（自增主键）
  `title` varchar(255) NOT NULL,                 -- 任务标题
  `description` text,                            -- 任务描述
  `status` varchar(50) NOT NULL,                 -- 任务状态（关联TaskStatus枚举）
  `creator_id` bigint(20) NOT NULL,              -- 创建人ID
  `assignee_id` bigint(20) DEFAULT NULL,         -- 执行人ID
  `error_message` varchar(255) DEFAULT NULL,     -- 异常信息
  `version` int(11) NOT NULL DEFAULT 1,          -- 版本号（乐观锁）
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP, -- 创建时间
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 更新时间
  PRIMARY KEY (`id`)
);

-- 用户表
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `role` varchar(50) NOT NULL,                -- LEADER / WORKER
  `password` varchar(255) NOT NULL,           -- BCrypt 哈希密码
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);

-- 种子用户数据（密码均为 password123，BCrypt 哈希）
INSERT INTO `user` (`id`, `name`, `role`, `password`) VALUES
(1001, 'Leader-张三', 'LEADER',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
(1002, 'Leader-李四', 'LEADER',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
(2001, 'Worker-王五', 'WORKER', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
(2002, 'Worker-赵六', 'WORKER', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
(2003, 'Worker-钱七', 'WORKER', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
(2004, 'Worker-孙八', 'WORKER', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
(2005, 'Worker-周九', 'WORKER', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');

-- Token 黑名单表（登出时注销 JWT）
CREATE TABLE `token_blacklist` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `jti` varchar(64) NOT NULL,                 -- JWT ID（唯一标识）
  `expires_at` timestamp NOT NULL,            -- Token 过期时间（用于清理）
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_jti` (`jti`)
);

-- 任务-候选工作者关联表
CREATE TABLE `task_candidate_worker` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,       -- 关联ID
  `task_id` bigint(20) NOT NULL,                 -- 任务ID（外键）
  `worker_id` bigint(20) NOT NULL,               -- 工作者ID（外键）
  PRIMARY KEY (`id`)
);
-- ============================================
-- community-admin 管理服务数据库初始化脚本
-- 数据库: community_admin
-- ============================================

CREATE DATABASE IF NOT EXISTS community_admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE community_admin;

-- 管理员表
CREATE TABLE IF NOT EXISTS `sys_admin` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码(MD5)',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
  `role` VARCHAR(20) DEFAULT 'ADMIN' COMMENT '角色',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统管理员表';

-- 插入默认管理员账号: admin / admin123 (MD5: e10adc3949ba59abbe56e057f20f883e)
INSERT INTO sys_admin (id, username, password, real_name, status, role, create_time)
VALUES (1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '超级管理员', 1, 'SUPER_ADMIN', NOW())
ON DUPLICATE KEY UPDATE username = VALUES(username);

-- 管理员操作日志表
CREATE TABLE IF NOT EXISTS `sys_admin_log` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `admin_id` BIGINT DEFAULT NULL COMMENT '管理员ID',
  `admin_name` VARCHAR(50) DEFAULT NULL COMMENT '管理员名称',
  `module` VARCHAR(50) DEFAULT NULL COMMENT '模块',
  `operation` VARCHAR(100) DEFAULT NULL COMMENT '操作描述',
  `method` VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
  `url` VARCHAR(255) DEFAULT NULL COMMENT '请求URL',
  `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `params` TEXT DEFAULT NULL COMMENT '请求参数',
  `time` BIGINT DEFAULT 0 COMMENT '耗时(ms)',
  `result` TINYINT DEFAULT 1 COMMENT '结果: 0-失败 1-成功',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_module` (`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员操作日志表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS `sys_config` (
  `id` VARCHAR(50) NOT NULL COMMENT '配置Key',
  `config_value` TEXT DEFAULT NULL COMMENT '配置值',
  `config_name` VARCHAR(100) DEFAULT NULL COMMENT '配置名称',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 初始化基础配置数据
INSERT INTO sys_config (id, config_value, config_name, remark) VALUES
('site.name', '社区论坛', '站点名称', '显示在页面标题的站点名称'),
('site.description', '一个现代化的社区论坛交流平台', '站点描述', '用于SEO的站点描述'),
('register.enabled', 'true', '开放注册', '是否允许用户自行注册'),
('post.audit.enabled', 'false', '发帖审核', '发帖后是否需要审核才能展示'),
('sensitive.word.enabled', 'true', '敏感词过滤', '是否启用敏感词过滤')
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);

-- ============================================
-- 安全库 database: community_security
-- ============================================

CREATE DATABASE IF NOT EXISTS community_security DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE community_security;

-- IP黑名单表
CREATE TABLE IF NOT EXISTS `ip_blacklist` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `ip` VARCHAR(45) NOT NULL COMMENT 'IP地址',
  `reason` VARCHAR(255) DEFAULT NULL COMMENT '封禁原因',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-已解除 1-生效中',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ip` (`ip`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IP黑名单表';

-- 用户操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
  `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
  `module` VARCHAR(50) DEFAULT NULL COMMENT '模块',
  `operation` VARCHAR(100) DEFAULT NULL COMMENT '操作',
  `method` VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
  `url` VARCHAR(255) DEFAULT NULL COMMENT '请求URL',
  `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `params` TEXT DEFAULT NULL COMMENT '请求参数',
  `time` BIGINT DEFAULT 0 COMMENT '耗时(ms)',
  `result` TINYINT DEFAULT 1 COMMENT '结果: 0-失败 1-成功',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_module` (`module`),
  KEY `idx_ip` (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户操作日志表';

-- 敏感词表
CREATE TABLE IF NOT EXISTS `sensitive_word` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `word` VARCHAR(100) NOT NULL COMMENT '敏感词',
  `level` TINYINT DEFAULT 1 COMMENT '级别: 1-一般 2-严重 3-极严重',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_word` (`word`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词表';

-- 文本内容审核记录表
CREATE TABLE IF NOT EXISTS `text_review` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `content_id` INT DEFAULT NULL COMMENT '内容ID',
  `content_type` TINYINT DEFAULT NULL COMMENT '内容类型: 1-帖子 2-评论',
  `original_content` TEXT DEFAULT NULL COMMENT '原始内容',
  `check_result` TINYINT DEFAULT 0 COMMENT '检测结果: 0-正常 1-敏感 2-违规',
  `sensitive_words` VARCHAR(500) DEFAULT NULL COMMENT '命中的敏感词',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_content_id` (`content_id`, `content_type`),
  KEY `idx_check_result` (`check_result`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文本审核记录表';

-- 举报表
CREATE TABLE IF NOT EXISTS `report` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `reporter_id` BIGINT NOT NULL COMMENT '举报人ID',
  `target_id` BIGINT NOT NULL COMMENT '被举报目标ID',
  `target_type` TINYINT NOT NULL COMMENT '目标类型: 1-帖子 2-评论 3-用户',
  `reason` VARCHAR(500) DEFAULT NULL COMMENT '举报原因',
  `status` TINYINT DEFAULT 0 COMMENT '状态: 0-待处理 1-已处理 2-已驳回',
  `handler_id` BIGINT DEFAULT NULL COMMENT '处理人ID',
  `handle_result` VARCHAR(500) DEFAULT NULL COMMENT '处理结果说明',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
  `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (`id`),
  KEY `idx_reporter_id` (`reporter_id`),
  KEY `idx_target_id` (`target_id`, `target_type`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表';

-- 限流记录表
CREATE TABLE IF NOT EXISTS `rate_limit_record` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `ip` VARCHAR(45) NOT NULL COMMENT 'IP地址',
  `endpoint` VARCHAR(100) DEFAULT NULL COMMENT '接口路径',
  `count` INT DEFAULT 0 COMMENT '访问次数',
  `window_start` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '窗口开始时间',
  PRIMARY KEY (`id`),
  KEY `idx_ip_endpoint` (`ip`, `endpoint`),
  KEY `idx_window_start` (`window_start`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='限流记录表';

-- =====================================================
-- 系统配置扩展 SQL
-- 添加新字段并插入扩展配置项
-- =====================================================

-- 1. 添加新字段（如果不存在）
ALTER TABLE `sys_config` ADD COLUMN IF NOT EXISTS `config_type` varchar(20) DEFAULT 'input' COMMENT '配置类型: input/switch/number/select/textarea' AFTER `config_name`;
ALTER TABLE `sys_config` ADD COLUMN IF NOT EXISTS `group_key` varchar(50) DEFAULT 'basic' COMMENT '配置分组' AFTER `config_type`;
ALTER TABLE `sys_config` ADD COLUMN IF NOT EXISTS `description` varchar(255) DEFAULT NULL COMMENT '配置描述' AFTER `group_key`;
ALTER TABLE `sys_config` ADD COLUMN IF NOT EXISTS `options` varchar(500) DEFAULT NULL COMMENT '选项值(逗号分隔)' AFTER `description`;

-- 2. 更新现有配置项，补充新字段
UPDATE `sys_config` SET `config_type`='input', `group_key`='basic', `description`='网站显示名称' WHERE `config_key`='site_name';
UPDATE `sys_config` SET `config_type`='input', `group_key`='basic', `description`='网站访问域名' WHERE `config_key`='site_domain';
UPDATE `sys_config` SET `config_type`='switch', `group_key`='security', `description`='是否允许新用户注册' WHERE `config_key`='allow_register';
UPDATE `sys_config` SET `config_type`='number', `group_key`='ai', `description`='每小时每用户AI调用次数上限' WHERE `config_key`='ai_rate_limit';
UPDATE `sys_config` SET `config_type`='switch', `group_key`='content', `description`='发布帖子是否需要管理员审核' WHERE `config_key`='post_audit';

-- 3. 插入新的扩展配置项
-- 基本设置 (basic)
INSERT IGNORE INTO `sys_config` (`config_key`, `config_value`, `config_name`, `config_type`, `group_key`, `description`, `options`) VALUES
('site_logo', '', '网站Logo', 'input', 'basic', '网站Logo图片URL', NULL),
('site_description', '技术交流社区', '网站描述', 'textarea', 'basic', '网站描述信息，用于SEO', NULL),
('site_icp', '', '备案号', 'input', 'basic', 'ICP备案号', NULL),
('site_notice', '', '网站公告', 'textarea', 'basic', '首页显示的公告内容', NULL);

-- 安全设置 (security)
INSERT IGNORE INTO `sys_config` (`config_key`, `config_value`, `config_name`, `config_type`, `group_key`, `description`, `options`) VALUES
('allow_register', 'true', '允许注册', 'switch', 'security', '是否允许新用户注册', NULL),
('register_captcha', 'true', '注册验证码', 'switch', 'security', '注册时是否需要验证码', NULL),
('login_captcha', 'false', '登录验证码', 'switch', 'security', '登录时是否需要验证码', NULL),
('password_min_length', '6', '密码最小长度', 'number', 'security', '用户密码的最小字符数', NULL),
('token_access_expire', '30', '访问令牌有效期', 'number', 'security', '用户访问令牌过期时间(分钟)', NULL),
('token_refresh_expire', '10080', '刷新令牌有效期', 'number', 'security', '用户刷新令牌过期时间(分钟, 默认7天=10080)', NULL),
('sensitive_filter', 'true', '敏感词过滤', 'switch', 'security', '是否开启内容敏感词过滤', NULL),
('sensitive_replace', '**', '敏感词替换', 'input', 'security', '敏感词替换为的文本', NULL);

-- 内容管理设置 (content)
INSERT IGNORE INTO `sys_config` (`config_key`, `config_value`, `config_name`, `config_type`, `group_key`, `description`, `options`) VALUES
('post_audit', 'false', '帖子审核', 'switch', 'content', '发布帖子是否需要管理员审核', NULL),
('comment_audit', 'false', '评论审核', 'switch', 'content', '发表评论是否需要审核', NULL),
('allow_anonymous', 'true', '允许匿名发帖', 'switch', 'content', '是否允许用户匿名发布帖子', NULL),
('post_title_max_length', '100', '帖子标题最大长度', 'number', 'content', '帖子标题允许的最大字符数', NULL),
('post_content_max_length', '20000', '帖子内容最大长度', 'number', 'content', '帖子内容允许的最大字符数', NULL),
('image_upload_limit', '5', '图片上传数量限制', 'number', 'content', '单个帖子最多上传图片数', NULL);

-- AI设置 (ai)
INSERT IGNORE INTO `sys_config` (`config_key`, `config_value`, `config_name`, `config_type`, `group_key`, `description`, `options`) VALUES
('ai_enabled', 'true', '开启AI功能', 'switch', 'ai', '是否开启AI助手功能', NULL),
('ai_review_post', 'false', 'AI审核帖子', 'switch', 'ai', '是否使用AI自动审核帖子内容', NULL),
('ai_rate_limit', '60', 'AI调用频率限制', 'number', 'ai', '每小时每用户AI调用次数上限', NULL),
('ai_model', 'gpt-3.5-turbo', '默认AI模型', 'input', 'ai', 'AI使用的默认模型名称', NULL),
('ai_max_tokens', '2048', 'AI最大输出Token', 'number', 'ai', 'AI回复最大Token数', NULL);

-- 界面显示设置 (ui)
INSERT IGNORE INTO `sys_config` (`config_key`, `config_value`, `config_name`, `config_type`, `group_key`, `description`, `options`) VALUES
('theme_mode', 'light', '默认主题', 'select', 'ui', '用户默认主题模式', 'light,dark,auto'),
('page_size', '10', '默认分页大小', 'number', 'ui', '列表页默认每页显示条数', '10,15,20,30,50'),
('hot_topic_count', '10', '热门话题数量', 'number', 'ui', '首页显示的热门话题数量', NULL);

-- 系统运行参数 (system)
INSERT IGNORE INTO `sys_config` (`config_key`, `config_value`, `config_name`, `config_type`, `group_key`, `description`, `options`) VALUES
('admin_token_expire', '1440', '管理员令牌有效期', 'number', 'system', '管理员访问令牌过期时间(分钟, 默认24小时=1440)', NULL),
('refresh_admin_token', 'true', '自动刷新管理员令牌', 'switch', 'system', '是否自动刷新管理员令牌', NULL),
('log_level', 'info', '日志级别', 'select', 'system', '系统日志记录级别', 'debug,info,warn,error'),
('max_upload_size', '10', '最大上传大小(MB)', 'number', 'system', '文件上传最大大小(MB)', NULL);

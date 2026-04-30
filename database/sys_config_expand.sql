-- =====================================================
-- 系统配置扩展 SQL（目标库：community_admin）
--
-- 用途：为「管理后台 → 系统配置」提供可选项，含站点/安全/内容/AI/UI/系统 等分组。
-- 其中 group_key='security' 的项对应前端「安全设置」分组（与 Spring Security、
-- community_security 库无关；安全业务数据仍在 community_security）。
--
-- 执行顺序建议：
--   1) 先执行 database/community_admin.sql 建表（标准 sys_config，含 config_key）
--   2) 再执行本文件（ALTER 扩展列 + INSERT IGNORE 种子数据）
-- =====================================================

-- 1. 添加新字段（如果不存在）
ALTER TABLE `sys_config` ADD COLUMN IF NOT EXISTS `config_type` varchar(20) DEFAULT 'input' COMMENT '配置类型: input/switch/number/select/textarea';
ALTER TABLE `sys_config` ADD COLUMN IF NOT EXISTS `group_key` varchar(50) DEFAULT 'basic' COMMENT '配置分组';
ALTER TABLE `sys_config` ADD COLUMN IF NOT EXISTS `description` varchar(255) DEFAULT NULL COMMENT '配置描述';
ALTER TABLE `sys_config` ADD COLUMN IF NOT EXISTS `options` varchar(500) DEFAULT NULL COMMENT '选项值(逗号分隔)';

-- 2. 更新现有配置项，补充新字段
UPDATE `sys_config` SET `config_type`='input', `group_key`='basic', `description`='热榜计算周期(小时)' WHERE `config_key`='hot_post_period';
UPDATE `sys_config` SET `config_type`='input', `group_key`='basic', `description`='热榜展示数量' WHERE `config_key`='hot_post_limit';
UPDATE `sys_config` SET `config_type`='input', `group_key`='basic', `description`='浏览量热度权重' WHERE `config_key`='hot_score_view';
UPDATE `sys_config` SET `config_type`='input', `group_key`='basic', `description`='点赞量热度权重' WHERE `config_key`='hot_score_like';
UPDATE `sys_config` SET `config_type`='input', `group_key`='basic', `description`='评论量热度权重' WHERE `config_key`='hot_score_comment';
UPDATE `sys_config` SET `config_type`='input', `group_key`='basic', `description`='分享量热度权重' WHERE `config_key`='hot_score_share';
UPDATE `sys_config` SET `config_type`='input', `group_key`='security', `description`='发帖限流次数' WHERE `config_key`='rate_limit_post';
UPDATE `sys_config` SET `config_type`='input', `group_key`='security', `description`='评论限流次数' WHERE `config_key`='rate_limit_comment';
UPDATE `sys_config` SET `config_type`='input', `group_key`='security', `description`='限流时间窗口(秒)' WHERE `config_key`='rate_limit_interval';
UPDATE `sys_config` SET `config_type`='input', `group_key`='content', `description`='签到获得积分' WHERE `config_key`='checkin_points';
UPDATE `sys_config` SET `config_type`='input', `group_key`='content', `description`='发帖获得积分' WHERE `config_key`='post_points';
UPDATE `sys_config` SET `config_type`='input', `group_key`='content', `description`='评论获得积分' WHERE `config_key`='comment_points';
UPDATE `sys_config` SET `config_type`='input', `group_key`='content', `description`='点赞获得积分' WHERE `config_key`='like_points';
UPDATE `sys_config` SET `config_type`='input', `group_key`='content', `description`='被关注获得积分' WHERE `config_key`='follow_points';
UPDATE `sys_config` SET `config_type`='switch', `group_key`='ai', `description`='是否启用AI摘要' WHERE `config_key`='ai_summary_enabled';
UPDATE `sys_config` SET `config_type`='switch', `group_key`='ai', `description`='是否启用AI审核' WHERE `config_key`='ai_review_enabled';
UPDATE `sys_config` SET `config_type`='switch', `group_key`='ai', `description`='是否启用AI推荐' WHERE `config_key`='ai_recommend_enabled';
UPDATE `sys_config` SET `config_type`='switch', `group_key`='security', `description`='是否启用敏感词过滤' WHERE `config_key`='sensitive_word_filter';
UPDATE `sys_config` SET `config_type`='switch', `group_key`='content', `description`='发帖是否需要审核' WHERE `config_key`='post_review_enabled';
UPDATE `sys_config` SET `config_type`='switch', `group_key`='content', `description`='评论是否需要审核' WHERE `config_key`='comment_review_enabled';

-- 3. 插入新的扩展配置项
-- 基本设置 (basic)
INSERT IGNORE INTO `sys_config` (`config_key`, `config_value`, `config_type`, `group_key`, `description`, `options`) VALUES
('site_name', '技术社区', 'input', 'basic', '网站名称', NULL),
('site_domain', '', 'input', 'basic', '网站域名', NULL),
('site_logo', '', 'input', 'basic', '网站Logo', NULL),
('site_description', '技术交流社区', 'textarea', 'basic', '网站描述', NULL),
('site_icp', '', 'input', 'basic', '备案号', NULL),
('site_notice', '', 'textarea', 'basic', '网站公告', NULL);

-- 安全设置 (security)
INSERT IGNORE INTO `sys_config` (`config_key`, `config_value`, `config_type`, `group_key`, `description`, `options`) VALUES
('allow_register', 'true', 'switch', 'security', '允许注册', NULL),
('register_captcha', 'true', 'switch', 'security', '注册验证码', NULL),
('login_captcha', 'false', 'switch', 'security', '登录验证码', NULL),
('password_min_length', '6', 'number', 'security', '密码最小长度', NULL),
('token_access_expire', '30', 'number', 'security', '访问令牌有效期(分钟)', NULL),
('token_refresh_expire', '10080', 'number', 'security', '刷新令牌有效期(分钟)', NULL),
('sensitive_filter', 'true', 'switch', 'security', '敏感词过滤', NULL),
('sensitive_replace', '**', 'input', 'security', '敏感词替换', NULL);

-- 内容管理设置 (content)
INSERT IGNORE INTO `sys_config` (`config_key`, `config_value`, `config_type`, `group_key`, `description`, `options`) VALUES
('post_audit', 'false', 'switch', 'content', '帖子审核', NULL),
('comment_audit', 'false', 'switch', 'content', '评论审核', NULL),
('allow_anonymous', 'true', 'switch', 'content', '允许匿名发帖', NULL),
('post_title_max_length', '100', 'number', 'content', '帖子标题最大长度', NULL),
('post_content_max_length', '20000', 'number', 'content', '帖子内容最大长度', NULL),
('image_upload_limit', '5', 'number', 'content', '图片上传数量限制', NULL);

-- AI设置 (ai)
INSERT IGNORE INTO `sys_config` (`config_key`, `config_value`, `config_type`, `group_key`, `description`, `options`) VALUES
('ai_enabled', 'true', 'switch', 'ai', '开启AI功能', NULL),
('ai_review_post', 'false', 'switch', 'ai', 'AI审核帖子', NULL),
('ai_rate_limit', '60', 'number', 'ai', 'AI调用频率限制', NULL),
('ai_model', 'gpt-3.5-turbo', 'input', 'ai', '默认AI模型', NULL),
('ai_max_tokens', '2048', 'number', 'ai', 'AI最大输出Token', NULL);

-- 界面显示设置 (ui)
INSERT IGNORE INTO `sys_config` (`config_key`, `config_value`, `config_type`, `group_key`, `description`, `options`) VALUES
('theme_mode', 'light', 'select', 'ui', '默认主题', 'light,dark,auto'),
('page_size', '10', 'number', 'ui', '默认分页大小', '10,15,20,30,50'),
('hot_topic_count', '10', 'number', 'ui', '热门话题数量', NULL);

-- 系统运行参数 (system)
INSERT IGNORE INTO `sys_config` (`config_key`, `config_value`, `config_type`, `group_key`, `description`, `options`) VALUES
('admin_token_expire', '1440', 'number', 'system', '管理员令牌有效期(分钟)', NULL),
('refresh_admin_token', 'true', 'switch', 'system', '自动刷新管理员令牌', NULL),
('log_level', 'info', 'select', 'system', '日志级别', 'debug,info,warn,error'),
('max_upload_size', '10', 'number', 'system', '最大上传大小(MB)', NULL);

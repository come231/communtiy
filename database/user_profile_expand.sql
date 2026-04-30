-- 用户个人主页扩展字段
-- 执行前请 USE community_user;

-- 1. 添加用户获赞总数、隐私设置字段
ALTER TABLE `user`
  ADD COLUMN `like_count` int DEFAULT '0' COMMENT '获赞总数（冗余统计）',
  ADD COLUMN `hide_likes` tinyint DEFAULT '0' COMMENT '是否隐藏喜欢列表: 0公开 1隐藏',
  ADD COLUMN `hide_collects` tinyint DEFAULT '0' COMMENT '是否隐藏收藏列表: 0公开 1隐藏',
  ADD COLUMN `bio_tags` varchar(500) DEFAULT '' COMMENT '个人标签，逗号分隔';

-- 2. 创建用户点赞记录表（用于查询用户的喜欢列表）
-- 注意：此表已存在 like_record，但需要补充查询索引
-- 已有 like_record 表包含: user_id, type, target_id, create_time

-- 为 like_record 表添加索引优化查询
ALTER TABLE `like_record`
  ADD KEY `idx_user_type_time` (`user_id`,`type`,`create_time` DESC);

-- 3. 创建用户个人标签表（如果后续需要更复杂的标签系统）
CREATE TABLE IF NOT EXISTS `user_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `tag_name` varchar(50) NOT NULL COMMENT '标签名称',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_tag` (`user_id`,`tag_name`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户个人标签表';

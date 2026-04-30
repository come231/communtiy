-- 创建板块成员表
CREATE TABLE IF NOT EXISTS `section_member` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `section_id` BIGINT(20) NOT NULL COMMENT '板块ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `role` TINYINT(4) DEFAULT 0 COMMENT '角色：0-成员，1-管理员，2-版主',
  `join_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `status` TINYINT(4) DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_section_user` (`section_id`, `user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_section_id` (`section_id`),
  CONSTRAINT `fk_section_member_section` FOREIGN KEY (`section_id`) REFERENCES `section` (`section_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='板块成员表（user_id 仅作业务引用，避免跨库无法建外键）';

-- 插入测试数据
INSERT INTO `section_member` (`section_id`, `user_id`, `role`, `join_time`, `status`) VALUES
(1, 1, 2, NOW(), 1), -- 版主
(2, 1, 0, NOW(), 1), -- 成员
(3, 1, 0, NOW(), 1); -- 成员
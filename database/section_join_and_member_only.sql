-- 执行本脚本后，可将 Section.java 中 memberOnlyPost 字段的 @TableField(exist = false) 去掉，以持久化该列；
-- 并在 SectionService.createSection 中按需恢复 section.setMemberOnlyPost(1)（新建板块默认仅成员发帖）。
--
-- 板块：仅成员可发帖（1=需加入板块；0=开放，沿用原 post_perm 语义）
ALTER TABLE `section`
  ADD COLUMN `member_only_post` TINYINT NOT NULL DEFAULT 0 COMMENT '1=仅版主与已加入成员可发帖' AFTER `post_perm`;

-- 加入申请（待版主审批）
CREATE TABLE IF NOT EXISTS `section_join_request` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `section_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待审 1通过 2拒绝',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_section_user` (`section_id`, `user_id`),
  KEY `idx_section_status` (`section_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='板块加入申请';

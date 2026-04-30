-- community_content.post：增加是否公开字段
-- 1=公开(默认), 0=私密(仅自己可见)

ALTER TABLE `post`
  ADD COLUMN `is_public` tinyint NOT NULL DEFAULT 1 COMMENT '是否公开: 1公开 0私密' AFTER `is_anonymous`;

-- 为已有数据设置默认值
UPDATE `post` SET `is_public` = 1 WHERE `is_public` IS NULL;

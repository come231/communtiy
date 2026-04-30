-- 帖子封面图（列表/卡片展示用）
ALTER TABLE `post` ADD COLUMN `cover_image` varchar(512) DEFAULT NULL COMMENT '封面图URL' AFTER `images`;

ALTER TABLE `post_draft` ADD COLUMN `cover_image` varchar(512) DEFAULT NULL COMMENT '封面图URL' AFTER `images`;

ALTER TABLE `post_draft` ADD COLUMN `is_anonymous` tinyint DEFAULT '0' COMMENT '草稿：是否匿名发布' AFTER `topic_ids`;

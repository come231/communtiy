-- =============================================================================
-- 业务扩展：帖子多图/多视频(整帖<=100MB)、评论配图与自定义表情、聊天附件(<=100MB)、帖子转文档
-- 执行前请备份；若某列/表已存在，跳过对应语句（或注释掉已执行过的段落）。
-- 库名与现有工程一致: community_file / community_content / community_message
-- =============================================================================

-- ----- community_content: post 扩展、post_media、comment 扩展、comment_media、post_document_export -----
USE `community_content`;

-- total_media_bytes / cover_file_id 放在 `video` 之后，不依赖是否已执行 post_cover_image.sql
ALTER TABLE `post`
  ADD COLUMN `total_media_bytes` bigint NOT NULL DEFAULT 0 COMMENT '本帖已关联媒体总大小(字节), 与 post_media 汇总一致' AFTER `video`,
  ADD COLUMN `cover_file_id` bigint DEFAULT NULL COMMENT '可选: 封面 file_record.id' AFTER `total_media_bytes`;

CREATE TABLE IF NOT EXISTS `post_media` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `file_id` bigint NOT NULL COMMENT 'community_file.file_record.id',
  `media_type` tinyint NOT NULL COMMENT '1=image 2=video',
  `size_bytes` bigint NOT NULL DEFAULT 0,
  `sort_order` int NOT NULL DEFAULT 0,
  `width` int DEFAULT NULL,
  `height` int DEFAULT NULL,
  `duration_ms` int DEFAULT NULL COMMENT '视频时长毫秒',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_post_sort` (`post_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子图片/视频';

ALTER TABLE `comment`
  MODIFY COLUMN `content` mediumtext NOT NULL COMMENT '文本/占位符(如{{m:1}})指向 comment_media',
  ADD COLUMN `content_mode` tinyint NOT NULL DEFAULT 0 COMMENT '0纯文本 1含自定义图/表情' AFTER `content`;

CREATE TABLE IF NOT EXISTS `comment_media` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `comment_id` bigint NOT NULL,
  `file_id` bigint NOT NULL,
  `media_kind` tinyint NOT NULL DEFAULT 1 COMMENT '1=贴纸或配图 2=其他',
  `sort_order` int NOT NULL DEFAULT 0,
  `size_bytes` bigint NOT NULL DEFAULT 0,
  `width` int DEFAULT NULL,
  `height` int DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_comment_id` (`comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论中自定义图片/表情资源';

CREATE TABLE IF NOT EXISTS `post_document_export` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `file_id` bigint NOT NULL COMMENT '生成的 doc/pdf 等 file_record.id',
  `format` varchar(16) NOT NULL,
  `include_video` tinyint NOT NULL DEFAULT 0 COMMENT '转文档为0(不含视频)',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0处理中 1成功 2失败',
  `error_message` varchar(500) DEFAULT NULL,
  `meta_json` json DEFAULT NULL COMMENT '图片列表/剔除视频数等',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `finish_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子导出文档记录';

-- 草稿与帖子能力对齐（可选）
-- ALTER TABLE `post_draft` ADD COLUMN `total_media_bytes` bigint NOT NULL DEFAULT 0 COMMENT '草稿媒体总字节' AFTER `video`;


-- ----- community_message: chat_msg 扩展、chat_msg_attachment -----
USE `community_message`;

ALTER TABLE `chat_msg`
  MODIFY COLUMN `content` mediumtext NOT NULL,
  ADD COLUMN `msg_type` tinyint NOT NULL DEFAULT 1 COMMENT '1=text 2=image 3=video 4=file 5=mixed' AFTER `to_user_id`,
  ADD COLUMN `total_attachment_bytes` bigint NOT NULL DEFAULT 0 COMMENT '本条消息附件总字节' AFTER `content`;

CREATE TABLE IF NOT EXISTS `chat_msg_attachment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `msg_id` bigint NOT NULL,
  `file_id` bigint NOT NULL,
  `att_type` tinyint NOT NULL COMMENT '1=image 2=video 3=file',
  `original_name` varchar(255) DEFAULT NULL,
  `size_bytes` bigint NOT NULL DEFAULT 0,
  `sort_order` int NOT NULL DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_msg_id` (`msg_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='聊天消息附件';

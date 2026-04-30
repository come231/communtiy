-- 添加收藏隐藏功能字段
ALTER TABLE collect ADD COLUMN is_hidden TINYINT(1) DEFAULT 0 COMMENT '是否隐藏 0-否 1-是';

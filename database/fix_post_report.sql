-- 添加举报描述字段（如果表已存在）
ALTER TABLE post_report ADD COLUMN description VARCHAR(500) DEFAULT NULL COMMENT '详细描述' AFTER reason;

-- AI 用户反馈表：用于提示词持续优化的数据闭环
CREATE TABLE IF NOT EXISTS ai_feedback (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    feedback_type VARCHAR(16) NOT NULL COMMENT '反馈类型: like/dislike',
    feedback_reason VARCHAR(255) NULL COMMENT '不满意原因',
    scene VARCHAR(32) NULL COMMENT '场景: create/edit/detail',
    operation VARCHAR(64) NULL COMMENT 'AI操作: generate/polish/summary等',
    user_id BIGINT NULL DEFAULT 0 COMMENT '用户ID（当前可空）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_feedback_type (feedback_type),
    INDEX idx_scene (scene),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI反馈数据表';

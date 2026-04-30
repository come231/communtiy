-- ai_check表添加人机回环数据飞轮字段

-- 人工复核状态
ALTER TABLE ai_check
    ADD COLUMN manual_review_status TINYINT DEFAULT 0 COMMENT '人工复核：0未复核 1确认AI正确 2纠正为违规 3纠正为正常',
    ADD COLUMN manual_label VARCHAR(100) DEFAULT NULL COMMENT '人工标签，如"严重违规-危化DIY"',
    ADD COLUMN manual_remark TEXT COMMENT '人工复核备注',
    ADD COLUMN reviewer_id BIGINT DEFAULT NULL COMMENT '复核管理员ID',
    ADD COLUMN review_time DATETIME DEFAULT NULL COMMENT '复核时间',
    ADD COLUMN in_vector_store TINYINT DEFAULT 0 COMMENT '是否已加入向量样本库：0未加入 1已加入';

-- 索引优化
CREATE INDEX idx_manual_status ON ai_check(manual_review_status);
CREATE INDEX idx_in_vector ON ai_check(in_vector_store);
CREATE INDEX idx_check_result_time ON ai_check(check_result, create_time);

package com.community.community.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensitiveFilterRecordTableInitializer {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        try {
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS community_security.sensitive_filter_record (
                        id BIGINT NOT NULL COMMENT '主键ID',
                        comment_id BIGINT DEFAULT NULL COMMENT '评论ID',
                        user_id BIGINT DEFAULT NULL COMMENT '评论用户ID',
                        username VARCHAR(100) DEFAULT NULL COMMENT '评论用户名称',
                        original_content TEXT COMMENT '原始评论内容',
                        filtered_content TEXT COMMENT '过滤后评论内容',
                        hit_words VARCHAR(1000) DEFAULT NULL COMMENT '命中敏感词(逗号分隔)',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        PRIMARY KEY (id),
                        KEY idx_comment_id (comment_id),
                        KEY idx_user_id (user_id),
                        KEY idx_create_time (create_time)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词过滤记录表'
                    """);
        } catch (Exception e) {
            log.error("初始化敏感词过滤记录表失败: {}", e.getMessage(), e);
        }
    }
}

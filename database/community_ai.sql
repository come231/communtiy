-- community_ai 数据库建表SQL
CREATE DATABASE IF NOT EXISTS community_ai DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE community_ai;

-- AI聊天记录表
CREATE TABLE ai_chat (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         user_id BIGINT NOT NULL COMMENT '用户ID',
                         session_id VARCHAR(64) NOT NULL COMMENT '会话ID',
                         role VARCHAR(20) NOT NULL COMMENT '角色: user/assistant',
                         content TEXT NOT NULL COMMENT '消息内容',
                         model VARCHAR(64) DEFAULT NULL COMMENT 'AI模型名',
                         tokens INT DEFAULT 0 COMMENT 'token数',
                         create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         INDEX idx_user_id (user_id),
                         INDEX idx_session_id (session_id),
                         INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI聊天记录表';

-- AI内容审核表
CREATE TABLE ai_check (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          user_id BIGINT DEFAULT NULL COMMENT '用户ID',
                          content_type INT DEFAULT 1 COMMENT '内容类型: 1帖子 2评论 3私信',
                          content_id BIGINT DEFAULT NULL COMMENT '内容ID',
                          original_content TEXT NOT NULL COMMENT '原始内容',
                          check_result INT DEFAULT 0 COMMENT '审核结果: 0正常 1敏感 2违规',
                          sensitive_words VARCHAR(500) DEFAULT NULL COMMENT '敏感词列表',
                          ai_suggestion VARCHAR(1000) DEFAULT NULL COMMENT 'AI建议',
                          create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          INDEX idx_content (content_type, content_id),
                          INDEX idx_result (check_result)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI内容审核表';

-- AI摘要表
CREATE TABLE ai_summary (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            target_id BIGINT NOT NULL COMMENT '目标ID',
                            target_type INT NOT NULL COMMENT '目标类型: 1帖子 2文章',
                            original_content TEXT NOT NULL COMMENT '原始内容',
                            summary_text TEXT DEFAULT NULL COMMENT '摘要文本',
                            keywords VARCHAR(500) DEFAULT NULL COMMENT '关键词JSON',
                            create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            INDEX idx_target (target_id, target_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI摘要表';

-- AI任务表
CREATE TABLE ai_task (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         user_id BIGINT DEFAULT NULL COMMENT '用户ID',
                         task_type INT NOT NULL COMMENT '任务类型: 1写作 2润色 3翻译 4总结',
                         input_content TEXT NOT NULL COMMENT '输入内容',
                         output_content TEXT DEFAULT NULL COMMENT '输出内容',
                         status INT DEFAULT 0 COMMENT '状态: 0进行中 1完成 2失败',
                         error_msg VARCHAR(1000) DEFAULT NULL COMMENT '错误信息',
                         create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         finish_time DATETIME DEFAULT NULL COMMENT '完成时间',
                         INDEX idx_user_task (user_id, task_type),
                         INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI异步任务表';

-- AI话题表
CREATE TABLE ai_topic (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          topic_name VARCHAR(100) NOT NULL COMMENT '话题名称',
                          description VARCHAR(500) DEFAULT NULL COMMENT '话题描述',
                          post_count INT DEFAULT 0 COMMENT '关联帖子数',
                          status INT DEFAULT 1 COMMENT '状态: 1正常 0禁用',
                          create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          UNIQUE KEY uk_topic_name (topic_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI话题表';

-- 用户兴趣标签表
CREATE TABLE user_interest (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_id BIGINT NOT NULL COMMENT '用户ID',
                               tag VARCHAR(50) NOT NULL COMMENT '兴趣标签',
                               weight DOUBLE DEFAULT 1.0 COMMENT '权重',
                               update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               UNIQUE KEY uk_user_tag (user_id, tag),
                               INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户兴趣标签表';

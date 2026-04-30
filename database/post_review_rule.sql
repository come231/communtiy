-- 帖子 AI 审核规则表（库：community_ai）
-- 执行前请 USE community_ai;

CREATE TABLE IF NOT EXISTS post_review_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    rule_name VARCHAR(200) DEFAULT NULL COMMENT '规则名称（展示用）',
    rule_text TEXT NOT NULL COMMENT '规则正文，将拼入 AI 提示词',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
    status INT NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status_sort (status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子AI审核规则配置';

-- 常用拦截/审核规则模板（可按平台调性增删；status=1 为启用）
-- 若已存在数据，可先 DELETE 或改为 INSERT IGNORE / 手工挑选执行
INSERT INTO post_review_rule (rule_name, rule_text, sort_order, status) VALUES
('法律法规', '不得发布违反国家法律法规的内容，包括但不限于煽动颠覆、分裂国家、宣扬恐怖主义与极端主义、传播淫秽色情、赌博、毒品、枪支弹药及其他管制物品相关信息。', 10, 1),
('暴力与危险', '不得详细描述或教唆暴力、自残、自杀方法；不得宣扬校园暴力、虐待；不得发布可能危及人身或公共安全的危险行为教程（如爆炸物、剧毒物制作等）。禁止以「思路讲解」「简易步骤」「日常材料组合」等形式教唆制作危险装置、私自改造危险器具、违规操作高风险物品或宣扬不顾安全后果的危险实验。', 20, 1),
('色情低俗', '不得发布露骨性行为描写、色情图片/视频链接暗示、招嫖或性交易信息；不得低俗擦边至明显性暗示且以挑逗为目的的内容。', 30, 1),
('未成年人保护', '不得侵害未成年人权益：恋童倾向、性化未成年人、胁迫未成年人、泄露未成年人隐私或诱导其线下见面等一律从严。', 40, 1),
('人身与歧视', '禁止针对个人或群体的侮辱、诽谤、人肉搜索、恶意挂人；禁止煽动地域、民族、性别、宗教、疾病等方面的歧视与仇恨。', 50, 1),
('谣言与虚假信息', '不得故意编造、传播可能扰乱公共秩序或侵害他人权益的谣言；涉及灾情、疫情、事故、政策等应以权威来源为准，明显不实且可能造成不良影响的从严。', 60, 1),
('诈骗与灰产', '不得发布刷单、套现、跑分、代办违法证件、假币、考试作弊器材、窃听窃照器材等违法交易或服务信息；不得钓鱼、仿冒官方或诱导转账的话术。', 70, 1),
('垃圾营销', '禁止刷屏式重复发帖、无意义灌水、大量外链引流、多级分销/传销话术、未标注的广告软文（若平台要求商业推广需审核或标注）。', 80, 1),
('隐私与数据', '未经允许不得公开他人手机号、住址、身份证号、行踪轨迹、私密聊天记录等可识别个人身份或隐私的信息。', 90, 1),
('侵权与抄袭', '不得侵犯他人著作权、商标权、肖像权、名誉权；大段搬运未授权内容或冒充他人名义发布。', 100, 1),
('政治与敏感话题', '涉及政治敏感、敏感历史事件、非法组织等话题应谨慎：煽动对立、歪曲史实、传播境外有害政治信息等内容从严处置。', 110, 1),
('医疗健康', '不得冒充医生或医疗机构给出诊断、处方或保证疗效；不得推广未批准的「神药」、违禁减肥药或违法医疗广告。', 120, 1),
('金融投资', '不得非法荐股、保证收益、诱导参与未经批准的虚拟货币 ICO、非法集资或高利贷；明显金融诈骗话术从严。', 130, 1);

-- 审核记录：违规程度百分比（AI 输出），便于排查
ALTER TABLE ai_check
    ADD COLUMN violation_percent DECIMAL(6,2) DEFAULT NULL COMMENT 'AI综合违规程度0-100，NULL表示未采用百分比输出' AFTER check_result;

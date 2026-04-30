package com.community.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.ai.entity.AiCheck;
import com.community.ai.mapper.AiCheckMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AiCheckLoader实现：从数据库加载历史严重违规样本
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiCheckLoaderImpl implements SemanticRiskDetector.AiCheckLoader {

    private final AiCheckMapper aiCheckMapper;

    @Override
    public List<SemanticRiskDetector.Sample> loadSevereCases(int limit) {
        // 加载以下三类样本：
        // 1. AI判定为违规(checkResult=2)且已人工确认正确(manualReviewStatus=1)
        // 2. 人工纠正为违规(manualReviewStatus=2)
        // 3. 已加入向量库的样本(inVectorStore=1)
        LambdaQueryWrapper<AiCheck> wrapper = new LambdaQueryWrapper<AiCheck>()
                .eq(AiCheck::getInVectorStore, 1)
                .or(w -> w
                        .eq(AiCheck::getCheckResult, 2)
                        .eq(AiCheck::getManualReviewStatus, 1))
                .or(w -> w
                        .eq(AiCheck::getManualReviewStatus, 2))
                .orderByDesc(AiCheck::getCreateTime)
                .last("LIMIT " + limit);

        List<AiCheck> records = aiCheckMapper.selectList(wrapper);

        return records.stream()
                .map(r -> new SemanticRiskDetector.Sample(
                        r.getId().toString(),
                        r.getOriginalContent(),
                        r.getManualLabel() != null ? r.getManualLabel() : "AI判定违规"
                ))
                .toList();
    }
}

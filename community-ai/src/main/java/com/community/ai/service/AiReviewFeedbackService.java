package com.community.ai.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.community.ai.entity.AiCheck;
import com.community.ai.mapper.AiCheckMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * AI审核反馈服务
 * 处理人工审核后的确认/纠正，驱动数据飞轮
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiReviewFeedbackService {

    private final AiCheckMapper aiCheckMapper;
    private final SemanticRiskDetector semanticRiskDetector;

    /**
     * 处理人工审核反馈
     * @param checkId ai_check.id
     * @param manualStatus 1=确认AI正确 2=纠正为违规 3=纠正为正常
     * @param label 标签分类，如"严重违规-危化DIY"、"误判-正常讨论"
     * @param remark 备注
     * @param reviewerId 审核员ID
     */
    @Transactional
    public void handleFeedback(Long checkId, Integer manualStatus, String label, String remark, Long reviewerId) {
        // 1. 更新审核记录
        LambdaUpdateWrapper<AiCheck> updateWrapper = new LambdaUpdateWrapper<AiCheck>()
                .eq(AiCheck::getId, checkId)
                .set(AiCheck::getManualReviewStatus, manualStatus)
                .set(AiCheck::getManualLabel, label)
                .set(AiCheck::getManualRemark, remark)
                .set(AiCheck::getReviewerId, reviewerId)
                .set(AiCheck::getReviewTime, LocalDateTime.now());

        // 人工纠正为违规时，同步修正check_result
        if (manualStatus == 2) {
            updateWrapper.set(AiCheck::getCheckResult, 2);
        } else if (manualStatus == 3) {
            // 人工纠正为正常
            updateWrapper.set(AiCheck::getCheckResult, 0);
        }

        int updated = aiCheckMapper.update(null, updateWrapper);
        if (updated == 0) {
            log.warn("审核反馈更新失败，记录不存在: checkId={}", checkId);
            return;
        }

        // 2. 数据飞轮：确认违规的样本加入向量库
        if (manualStatus == 2) { // 人工纠正为违规
            AiCheck record = aiCheckMapper.selectById(checkId);
            if (record != null && record.getOriginalContent() != null) {
                String finalLabel = (label != null && !label.isEmpty())
                        ? label
                        : "人工纠正-严重违规";

                semanticRiskDetector.feedback(
                        checkId.toString(),
                        record.getOriginalContent(),
                        true,
                        finalLabel
                );

                // 标记为已入库
                markInVectorStore(checkId);

                log.info("数据飞轮：人工违规样本入库 id={}, label={}", checkId, finalLabel);
            }
        }

        // 3. 日志记录
        String action = switch (manualStatus) {
            case 1 -> "确认AI正确";
            case 2 -> "纠正为违规";
            case 3 -> "纠正为正常";
            default -> "未知操作";
        };
        log.info("人工审核反馈: checkId={}, action={}, reviewer={}", checkId, action, reviewerId);
    }

    /**
     * 标记记录已加入向量库
     */
    public void markInVectorStore(Long checkId) {
        LambdaUpdateWrapper<AiCheck> wrapper = new LambdaUpdateWrapper<AiCheck>()
                .eq(AiCheck::getId, checkId)
                .set(AiCheck::getInVectorStore, 1);
        aiCheckMapper.update(null, wrapper);
    }

    /**
     * 获取向量库大小
     */
    public int getVectorStoreSize() {
        // 通过查询in_vector_store=1的记录数
        Long count = aiCheckMapper.selectCount(
                new LambdaUpdateWrapper<AiCheck>().eq(AiCheck::getInVectorStore, 1)
        );
        return count != null ? count.intValue() : 0;
    }
}

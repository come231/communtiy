package com.community.ai.consumer;

import com.community.ai.entity.AiTask;
import com.community.ai.mapper.AiTaskMapper;
import com.community.ai.util.AiCallHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiTaskConsumer {

    private final AiCallHelper aiCallHelper;
    private final AiTaskMapper aiTaskMapper;

    @RabbitListener(queues = "ai.task.queue")
    public void handleAiTask(Map<String, Object> taskMessage) {
        log.info("收到异步AI任务: {}", taskMessage);

        Long taskId = Long.valueOf(taskMessage.get("taskId").toString());
        AiTask task = aiTaskMapper.selectById(taskId);
        if (task == null) {
            log.error("任务不存在, taskId={}", taskId);
            return;
        }

        try {
            String prompt = buildPrompt(task.getTaskType(), task.getInputContent());
            String result = aiCallHelper.call(prompt);

            task.setOutputContent(result);
            task.setStatus(1);
            task.setFinishTime(LocalDateTime.now());
            aiTaskMapper.updateById(task);
            log.info("AI任务完成, taskId={}", taskId);
        } catch (Exception e) {
            log.error("AI任务执行失败, taskId={}, error={}", taskId, e.getMessage(), e);
            task.setStatus(2);
            task.setErrorMsg(e.getMessage());
            task.setFinishTime(LocalDateTime.now());
            aiTaskMapper.updateById(task);
        }
    }

    private String buildPrompt(Integer taskType, String inputContent) {
        return switch (taskType) {
            case 1 -> "请根据以下要求写一篇文章：" + inputContent;
            case 2 -> "请润色以下内容：" + inputContent;
            case 3 -> "请翻译以下内容为英文：" + inputContent;
            case 4 -> "请总结以下内容：" + inputContent;
            default -> "请处理以下内容：" + inputContent;
        };
    }
}

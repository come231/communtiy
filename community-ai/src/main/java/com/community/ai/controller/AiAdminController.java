package com.community.ai.controller;

import com.community.ai.entity.AiTopic;
import com.community.ai.service.AiTopicService;
import com.community.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai/admin")
@RequiredArgsConstructor
@Tag(name = "AI话题管理", description = "AI话题管理接口")
public class AiAdminController {

    private final AiTopicService aiTopicService;

    @GetMapping("/topics")
    @Operation(summary = "话题列表", description = "获取AI推荐话题列表")
    public Result<List<AiTopic>> getTopics() {
        List<AiTopic> topics = aiTopicService.getHotTopics();
        return Result.success(topics);
    }

    @PostMapping("/generate-topics")
    @Operation(summary = "生成话题", description = "AI生成推荐话题")
    public Result<List<String>> generateTopics(@RequestBody(required = false) Map<String, String> params) {
        String keywords = params != null ? params.get("keywords") : null;
        List<String> topics = aiTopicService.generateTopics(keywords);
        return Result.success(topics);
    }

    @PostMapping("/topic")
    @Operation(summary = "创建话题", description = "手动创建AI话题")
    public Result<AiTopic> createTopic(@RequestBody Map<String, String> params) {
        String name = params.get("name");
        String description = params.get("description");
        AiTopic topic = aiTopicService.createTopic(name, description);
        return Result.success(topic);
    }
}

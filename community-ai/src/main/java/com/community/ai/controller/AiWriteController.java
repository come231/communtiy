package com.community.ai.controller;

import com.community.ai.dto.WriteRequest;
import com.community.ai.service.AiWriteService;
import com.community.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai/write")
@RequiredArgsConstructor
@Tag(name = "AI写作", description = "AI辅助写作接口")
public class AiWriteController {

    private final AiWriteService aiWriteService;

    @PostMapping("/article")
    @Operation(summary = "生成文章", description = "根据标题和要求生成文章")
    public Result<String> writeArticle(@RequestBody WriteRequest request) {
        String result = aiWriteService.writeArticle(request.getTitle(), request.getRequirements());
        return Result.success(result);
    }

    @PostMapping("/polish")
    @Operation(summary = "内容润色", description = "对文章内容进行润色优化")
    public Result<String> polishContent(@RequestBody WriteRequest request) {
        String result = aiWriteService.polishContent(request.getContent());
        return Result.success(result);
    }

    @PostMapping("/translate")
    @Operation(summary = "内容翻译", description = "将内容翻译为目标语言")
    public Result<String> translateContent(@RequestBody WriteRequest request) {
        String result = aiWriteService.translateContent(request.getContent(), request.getTargetLang());
        return Result.success(result);
    }

    @PostMapping("/summarize")
    @Operation(summary = "内容总结", description = "对文章内容进行总结")
    public Result<String> summarizeContent(@RequestBody WriteRequest request) {
        String result = aiWriteService.summarizeContent(request.getContent());
        return Result.success(result);
    }

    @PostMapping("/post/generate")
    @Operation(summary = "社区帖子一键生成", description = "按社区板块和关键词生成可发布帖子")
    public Result<String> generatePost(@RequestBody WriteRequest request) {
        return Result.success(aiWriteService.generateCommunityPost(request));
    }

    @PostMapping("/post/complete")
    @Operation(summary = "社区帖子实时补全", description = "根据输入前缀生成短句补全")
    public Result<String> completePost(@RequestBody WriteRequest request) {
        return Result.success(aiWriteService.completeCommunityPost(request));
    }

    @PostMapping("/post/continue")
    @Operation(summary = "社区帖子AI续写", description = "基于已写内容续写下一段")
    public Result<String> continuePost(@RequestBody WriteRequest request) {
        return Result.success(aiWriteService.continueCommunityPost(request));
    }

    @PostMapping("/post/polish")
    @Operation(summary = "社区帖子AI润色", description = "对帖子进行一键或定向润色")
    public Result<String> polishPost(@RequestBody WriteRequest request) {
        return Result.success(aiWriteService.polishCommunityPost(request));
    }

    @PostMapping("/post/summary")
    @Operation(summary = "帖子摘要", description = "查看帖子时生成30-50字摘要")
    public Result<String> summarizePost(@RequestBody WriteRequest request) {
        return Result.success(aiWriteService.summarizePostForReading(request));
    }

    @PostMapping("/post/keypoints")
    @Operation(summary = "解读帖子重点", description = "提炼帖子核心关键信息")
    public Result<String> postKeyPoints(@RequestBody WriteRequest request) {
        return Result.success(aiWriteService.extractPostKeyPoints(request));
    }

    @PostMapping("/post/reply-suggestions")
    @Operation(summary = "回复建议", description = "根据帖子内容生成可直接发送的回复建议")
    public Result<String> replySuggestions(@RequestBody WriteRequest request) {
        return Result.success(aiWriteService.suggestPostReply(request));
    }

    @PostMapping("/feedback")
    @Operation(summary = "AI反馈", description = "记录用户对AI结果的满意度反馈")
    public Result<Boolean> feedback(@RequestBody WriteRequest request) {
        aiWriteService.submitFeedback(request);
        return Result.success(Boolean.TRUE);
    }
}

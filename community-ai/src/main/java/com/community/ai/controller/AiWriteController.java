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
}

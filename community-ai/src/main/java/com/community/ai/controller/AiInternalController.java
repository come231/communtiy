package com.community.ai.controller;

import com.community.ai.service.AiReviewService;
import com.community.common.result.Result;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务间调用（绑定审核记录与业务 ID 等）
 */
@Hidden
@RestController
@RequestMapping("/ai/internal")
@RequiredArgsConstructor
public class AiInternalController {

    private final AiReviewService aiReviewService;

    @PutMapping("/check/{checkId}/post/{postId}")
    public Result<Void> bindCheckToPost(@PathVariable Long checkId, @PathVariable Long postId) {
        aiReviewService.bindCheckToPost(checkId, postId);
        return Result.success();
    }
}

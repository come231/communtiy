package com.community.admin.controller;

import com.community.admin.service.SensitiveWordService;
import com.community.common.result.Result;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 内网/服务间：敏感词全量，供其他微服务拉取后内存过滤（生产可再加鉴权或走网关白名单+IP 限制）
 */
@Hidden
@RestController
@RequestMapping("/admin/internal/sensitive-words")
@Tag(name = "内部-敏感词")
@RequiredArgsConstructor
public class InternalSensitiveWordController {

    private final SensitiveWordService sensitiveWordService;

    @GetMapping("/lexicon")
    public Result<List<String>> lexicon() {
        return Result.success(sensitiveWordService.listAllActiveWordStrings());
    }
}

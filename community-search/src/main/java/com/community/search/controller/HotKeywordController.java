package com.community.search.controller;

import com.community.common.result.Result;
import com.community.search.entity.SearchHotKeyword;
import com.community.search.service.HotKeywordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/hot-keyword")
@RequiredArgsConstructor
@Tag(name = "热词管理", description = "搜索热词接口")
public class HotKeywordController {

    private final HotKeywordService hotKeywordService;

    @GetMapping("/top")
    @Operation(summary = "热门搜索词", description = "获取热门搜索词列表")
    public Result<List<SearchHotKeyword>> getTopKeywords(
            @Parameter(description = "返回数量") @RequestParam(defaultValue = "10") int limit) {
        List<SearchHotKeyword> keywords = hotKeywordService.getTopKeywords(limit);
        return Result.success(keywords);
    }
}

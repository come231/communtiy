package com.community.search.controller;

import com.community.common.result.PageResult;
import com.community.common.result.Result;
import com.community.common.util.ContextHolder;
import com.community.search.dto.PostSearchDTO;
import com.community.search.entity.SearchHotKeyword;
import com.community.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Tag(name = "搜索管理", description = "帖子搜索、热词接口")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/posts")
    @Operation(summary = "搜索帖子", description = "根据关键词搜索帖子")
    public Result<PageResult<PostSearchDTO>> searchPosts(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {

        if (keyword != null && !keyword.trim().isEmpty()) {
            searchService.saveSearchHistory(ContextHolder.getUserId(), keyword);
        }

        PageResult<PostSearchDTO> result = searchService.searchPosts(keyword, page, size);
        return Result.success(result);
    }

    @GetMapping("/hot-keywords")
    @Operation(summary = "热门关键词", description = "获取搜索热词列表")
    public Result<List<SearchHotKeyword>> getHotKeywords() {
        List<SearchHotKeyword> keywords = searchService.getHotKeywords();
        return Result.success(keywords);
    }

    @GetMapping("/history")
    @Operation(summary = "搜索历史", description = "获取当前用户的搜索历史")
    public Result<List<String>> getSearchHistory() {
        List<String> history = searchService.getSearchHistory(ContextHolder.getUserId());
        return Result.success(history);
    }

    @PostMapping("/clear-history")
    @Operation(summary = "清除搜索历史", description = "清除当前用户的搜索历史")
    public Result<Void> clearSearchHistory() {
        searchService.clearSearchHistory(ContextHolder.getUserId());
        return Result.success();
    }
}

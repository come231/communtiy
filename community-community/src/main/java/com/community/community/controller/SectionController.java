package com.community.community.controller;

import com.community.common.result.Result;
import com.community.community.entity.Section;
import com.community.community.service.SectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sections")
@RequiredArgsConstructor
@Tag(name = "板块管理", description = "社区板块接口")
public class SectionController {

    private final SectionService sectionService;

    @GetMapping
    @Operation(summary = "板块列表", description = "获取所有板块列表")
    public Result<List<Section>> listSections() {
        return Result.success(sectionService.listAllSections());
    }

    @GetMapping("/parents")
    @Operation(summary = "父板块列表", description = "获取父级板块列表")
    public Result<List<Section>> listParentSections() {
        return Result.success(sectionService.listParentSections());
    }

    @GetMapping("/{sectionId}")
    @Operation(summary = "板块详情", description = "获取指定板块的详细信息")
    public Result<Section> getSectionDetail(@PathVariable Long sectionId) {
        return Result.success(sectionService.getSectionDetail(sectionId));
    }

    @PostMapping
    @Operation(summary = "创建板块", description = "用户自定义创建板块")
    public Result<Section> createSection(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String intro = body.get("intro");
        String icon = body.get("icon");
        return Result.success(sectionService.createSection(name, intro, icon));
    }
}

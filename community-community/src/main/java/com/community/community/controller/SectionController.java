package com.community.community.controller;

import com.community.common.result.PageResult;
import com.community.common.result.Result;
import com.community.common.util.ContextHolder;
import com.community.community.dto.SectionViewVO;
import com.community.community.entity.Section;
import com.community.community.entity.SectionCategory;
import com.community.community.entity.SectionJoinRequest;
import com.community.community.entity.SectionMember;
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

    @GetMapping("/categories")
    @Operation(summary = "板块分类列表", description = "获取所有板块分类列表")
    public Result<List<SectionCategory>> listCategories() {
        return Result.success(sectionService.listAllCategories());
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

    @GetMapping("/{sectionId}/context")
    @Operation(summary = "板块视图上下文", description = "成员数、是否可发帖、加入申请状态等（需登录时带用户态）")
    public Result<SectionViewVO> getSectionContext(@PathVariable Long sectionId) {
        Long uid = ContextHolder.getUserId();
        return Result.success(sectionService.getSectionView(sectionId, uid));
    }

    @GetMapping("/{sectionId}/members")
    @Operation(summary = "板块成员分页", description = "分页获取板块成员列表")
    public Result<PageResult<SectionMember>> pageMembers(
            @PathVariable Long sectionId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(sectionService.pageMembers(sectionId, pageNum, pageSize));
    }

    @PostMapping("/{sectionId}/join")
    @Operation(summary = "申请加入板块", description = "memberOnlyPost=1 时需申请，由版主审批")
    public Result<Void> applyJoin(@PathVariable Long sectionId) {
        sectionService.applyJoin(sectionId, ContextHolder.getUserId());
        return Result.success();
    }

    @GetMapping("/{sectionId}/join-requests")
    @Operation(summary = "待审加入申请", description = "版主查看待审列表")
    public Result<java.util.List<SectionJoinRequest>> pendingJoinRequests(@PathVariable Long sectionId) {
        return Result.success(sectionService.listPendingJoinRequests(sectionId, ContextHolder.getUserId()));
    }

    @PostMapping("/{sectionId}/join-requests/{userId}/approve")
    @Operation(summary = "通过加入申请", description = "版主操作")
    public Result<Void> approveJoin(@PathVariable Long sectionId, @PathVariable Long userId) {
        sectionService.approveJoin(sectionId, userId, ContextHolder.getUserId());
        return Result.success();
    }

    @PostMapping("/{sectionId}/join-requests/{userId}/reject")
    @Operation(summary = "拒绝加入申请", description = "版主操作")
    public Result<Void> rejectJoin(@PathVariable Long sectionId, @PathVariable Long userId) {
        sectionService.rejectJoin(sectionId, userId, ContextHolder.getUserId());
        return Result.success();
    }

    @DeleteMapping("/{sectionId}/members/{userId}")
    @Operation(summary = "移出成员", description = "版主将成员移出板块")
    public Result<Void> kickMember(@PathVariable Long sectionId, @PathVariable Long userId) {
        sectionService.kickMember(sectionId, userId, ContextHolder.getUserId());
        return Result.success();
    }

    @PostMapping
    @Operation(summary = "创建板块", description = "用户自定义创建板块，支持选择现有分类或自定义分类")
    public Result<Section> createSection(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String intro = (String) body.get("intro");
        String icon = (String) body.get("icon");
        Integer status = body.get("status") != null ? ((Number) body.get("status")).intValue() : 1;

        // 分类参数：categoryId为现有分类ID，customCategory为自定义分类名称
        Long categoryId = null;
        if (body.get("categoryId") != null) {
            categoryId = ((Number) body.get("categoryId")).longValue();
        }
        String customCategory = (String) body.get("customCategory");

        return Result.success(sectionService.createSection(name, intro, icon, status, categoryId, customCategory));
    }

    @GetMapping("/user")
    @Operation(summary = "用户板块", description = "获取当前用户创建的板块")
    public Result<List<Section>> listUserSections() {
        Long userId = ContextHolder.getUserId();
        return Result.success(sectionService.listUserSections(userId));
    }

    @GetMapping("/user/joined")
    @Operation(summary = "我加入的板块", description = "获取当前用户加入的板块")
    public Result<List<Section>> listJoinedSections() {
        Long userId = ContextHolder.getUserId();
        return Result.success(sectionService.listJoinedSections(userId));
    }
}

package com.community.interact.controller;

import com.community.common.result.Result;
import com.community.common.util.ContextHolder;
import com.community.interact.entity.CollectFolder;
import com.community.interact.service.CollectFolderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
@Tag(name = "收藏夹管理", description = "收藏文件夹接口")
public class FolderController {

    private final CollectFolderService collectFolderService;

    @GetMapping
    @Operation(summary = "收藏夹列表", description = "获取当前用户的收藏文件夹列表")
    public Result<List<CollectFolder>> listFolders() {
        return Result.success(collectFolderService.listFolders(ContextHolder.getUserId()));
    }

    @PostMapping
    @Operation(summary = "创建收藏夹", description = "创建新的收藏文件夹")
    public Result<CollectFolder> createFolder(@RequestBody Map<String, String> body) {
        return Result.success(collectFolderService.createFolder(
                ContextHolder.getUserId(), body.get("name")));
    }

    @DeleteMapping("/{folderId}")
    @Operation(summary = "删除收藏夹", description = "删除指定的收藏文件夹")
    public Result<Void> deleteFolder(@PathVariable Long folderId) {
        collectFolderService.deleteFolder(ContextHolder.getUserId(), folderId);
        return Result.success();
    }
}

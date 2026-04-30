package com.community.file.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.dto.FileInfoDTO;
import com.community.common.dto.GcOrphansRequest;
import com.community.common.dto.GcOrphansResult;
import com.community.common.result.Result;
import com.community.file.config.FileGcProperties;
import com.community.file.entity.FileRecord;
import com.community.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Tag(name = "文件管理", description = "文件上传、下载、删除等接口")
public class FileController {

    private final FileService fileService;
    private final FileGcProperties fileGcProperties;

    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "naming=avatar 时对象键为 {biz}/{userId}{ext} 并覆盖同用户旧文件，需 uploaderId 或 X-User-Id；私有 Bucket 不返回永久 url")
    public Result<?> upload(@RequestParam("file") MultipartFile file,
                             @Parameter(description = "上传者ID") @RequestParam(value = "uploaderId", required = false) Long uploaderId,
                             @Parameter(description = "业务目录，默认 file") @RequestParam(value = "biz", required = false) String biz,
                             @Parameter(description = "avatar=用户id+后缀 固定覆盖") @RequestParam(value = "naming", required = false) String naming,
                             @RequestHeader(value = "X-User-Id", required = false) String xUserId) {
        Long uid = uploaderId;
        if (uid == null && StringUtils.hasText(xUserId)) {
            try {
                uid = Long.parseLong(xUserId.trim());
            } catch (NumberFormatException ignored) {
                uid = null;
            }
        }
        boolean avatarKey = "avatar".equalsIgnoreCase(naming);
        if (avatarKey && (uid == null || uid <= 0)) {
            return Result.error("头像规范上传需要 uploaderId 或 X-User-Id");
        }
        String b = StringUtils.hasText(biz) ? biz : (avatarKey ? "community/images/avatar" : "file");
        FileRecord record = fileService.uploadFile(file, uid, b, avatarKey);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", String.valueOf(record.getId()));
        data.put("filePath", record.getStoredName());
        data.put("mimeType", record.getMimeType());
        if (StringUtils.hasText(record.getUrl())) {
            data.put("url", record.getUrl());
        } else {
            data.put("url", null);
        }
        return Result.success(data);
    }

    @GetMapping("/download/{id}")
    @Operation(summary = "下载/预览", description = "OSS：302 到预签名或公网；本地则流式输出")
    public void download(@PathVariable("id") Long id, HttpServletResponse response) throws java.io.IOException {
        FileRecord record = fileService.getFileInfo(id);
        if (fileService.isOssRecord(record)) {
            String loc = fileService.getOssHttpGetUrl(record);
            if (StringUtils.hasText(loc)) {
                response.setStatus(HttpServletResponse.SC_FOUND);
                response.setHeader("Location", loc);
                return;
            }
        }
        byte[] data = fileService.downloadFile(id);

        response.setContentType(record.getMimeType());
        response.setHeader("Content-Disposition", "attachment;filename=\"" + record.getOriginalName() + "\"");
        response.setContentLength(data.length);
        try {
            response.getOutputStream().write(data);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("文件下载失败: {}", id, e);
            throw new RuntimeException("文件下载失败");
        }
    }

    @GetMapping("/list")
    @Operation(summary = "文件列表", description = "分页获取文件列表")
    public Result<Page<FileRecord>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        Page<FileRecord> result = fileService.getFileList(page, size);
        return Result.success(result);
    }

    @GetMapping("/info/{id}")
    @Operation(summary = "文件详情", description = "获取文件详细信息")
    public Result<FileRecord> info(@PathVariable("id") Long id) {
        FileRecord record = fileService.getFileInfo(id);
        return Result.success(record);
    }

    @PostMapping("/infos")
    @Operation(summary = "批量文件元信息", description = "按 fileId 列表返回 id、url、size 等，供业务服务做容量与鉴权")
    public Result<List<FileInfoDTO>> listInfos(@RequestBody List<Long> fileIds) {
        return Result.success(fileService.listFileInfoByIds(fileIds));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除文件", description = "根据文件ID删除文件")
    public Result<?> delete(@PathVariable("id") Long id) {
        fileService.deleteFile(id);
        return Result.success(null);
    }

    @GetMapping("/my-uploads")
    @Operation(summary = "我的上传", description = "获取当前用户的上传记录")
    public Result<?> myUploads(@Parameter(description = "上传者ID") @RequestParam("uploaderId") Long uploaderId) {
        var list = fileService.getFilesByUploader(uploaderId);
        return Result.success(list);
    }

    @PostMapping("/internal/gc/orphans")
    @Operation(summary = "内部-无引用文件清理", description = "需 X-Internal-Token 与 community.file.gc.internal-token 一致；仅删早于 minAgeHours 的库行且不在已引用集合中的资源")
    public Result<GcOrphansResult> garbageCollectOrphans(
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            @RequestBody GcOrphansRequest request) {
        if (!StringUtils.hasText(fileGcProperties.getInternalToken()) || !fileGcProperties.getInternalToken().equals(token)) {
            return Result.error("未授权");
        }
        if (request == null) {
            request = new GcOrphansRequest();
        }
        int minH = request.getMinAgeHours() != null && request.getMinAgeHours() > 0
                ? request.getMinAgeHours() : 24;
        Set<Long> ref = new HashSet<>();
        if (request.getReferencedFileIds() != null) {
            ref.addAll(request.getReferencedFileIds());
        }
        int n = fileService.deleteOrphanFilesNotReferenced(ref, minH);
        return Result.success(new GcOrphansResult(n));
    }
}

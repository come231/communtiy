package com.community.file.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.result.Result;
import com.community.file.entity.FileRecord;
import com.community.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Tag(name = "文件管理", description = "文件上传、下载、删除等接口")
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传文件到服务器")
    public Result<?> upload(@RequestParam("file") MultipartFile file,
                             @Parameter(description = "上传者ID") @RequestParam(value = "uploaderId", required = false) Long uploaderId) {
        FileRecord record = fileService.uploadFile(file, uploaderId);
        return Result.success(record);
    }

    @GetMapping("/download/{id}")
    @Operation(summary = "下载文件", description = "根据文件ID下载文件")
    public void download(@PathVariable("id") Long id, HttpServletResponse response) {
        FileRecord record = fileService.getFileInfo(id);
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
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "文件类型") @RequestParam(required = false) Integer fileType) {
        Page<FileRecord> result = fileService.getFileList(page, size, fileType);
        return Result.success(result);
    }

    @GetMapping("/info/{id}")
    @Operation(summary = "文件详情", description = "获取文件详细信息")
    public Result<FileRecord> info(@PathVariable("id") Long id) {
        FileRecord record = fileService.getFileInfo(id);
        return Result.success(record);
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
}

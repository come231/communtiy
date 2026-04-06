package com.community.file.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.util.SnowflakeIdWorker;
import com.community.file.config.StorageConfig;
import com.community.file.entity.FileRecord;
import com.community.file.mapper.FileRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FileService {

    private final FileRecordMapper fileRecordMapper;
    private final StorageConfig storageConfig;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    /**
     * 上传文件
     */
    public FileRecord uploadFile(MultipartFile file, Long uploaderId) {
        // 校验文件非空
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        // 校验文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("文件大小不能超过10MB");
        }

        // 获取MIME类型并判断文件类型
        String contentType = file.getContentType();
        int fileType = determineFileType(contentType);
        String originalName = file.getOriginalFilename();

        // 生成存储路径: uploads/{yyyy/MMdd}/{UUID}_{原文件名}
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MMdd"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String fileName = uuid + "_" + originalName;
        String relativePath = "uploads/" + datePath + "/" + fileName;
        Path fullPath = Paths.get(storageConfig.getPath(), datePath, fileName);

        try {
            // 确保目录存在
            Files.createDirectories(fullPath.getParent());
            // 保存文件到磁盘
            file.transferTo(fullPath.toFile());
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new RuntimeException("文件保存失败: " + e.getMessage());
        }

        // 记录数据库
        FileRecord record = new FileRecord();
        record.setId(SnowflakeIdWorker.nextId());
        record.setFileName(fileName);
        record.setOriginalName(originalName);
        record.setFilePath(relativePath);
        record.setFileSize(file.getSize());
        record.setMimeType(contentType);
        record.setFileType(fileType);
        record.setUploaderId(uploaderId);
        record.setCreateTime(LocalDateTime.now());

        fileRecordMapper.insert(record);

        log.info("文件上传成功: {}, fileId: {}", originalName, record.getId());

        return record;
    }

    /**
     * 下载文件，返回字节数组
     */
    public byte[] downloadFile(Long fileId) {
        FileRecord record = fileRecordMapper.selectById(fileId);
        if (record == null) {
            throw new RuntimeException("文件不存在");
        }
        Path fullPath = Paths.get(record.getFilePath()).isAbsolute()
                ? Paths.get(record.getFilePath())
                : Paths.get(storageConfig.getPath()).resolveSibling(record.getFilePath());

        try {
            return Files.readAllBytes(fullPath);
        } catch (IOException e) {
            log.error("文件读取失败: {}", fileId, e);
            throw new RuntimeException("文件读取失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件记录信息
     */
    public FileRecord getFileInfo(Long fileId) {
        FileRecord record = fileRecordMapper.selectById(fileId);
        if (record == null) {
            throw new RuntimeException("文件不存在");
        }
        return record;
    }

    /**
     * 分页查询文件列表（可按类型筛选）
     */
    public Page<FileRecord> getFileList(int page, int size, Integer fileType) {
        Page<FileRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<FileRecord> wrapper = new LambdaQueryWrapper<>();
        if (fileType != null && fileType > 0) {
            wrapper.eq(FileRecord::getFileType, fileType);
        }
        wrapper.orderByDesc(FileRecord::getCreateTime);
        return fileRecordMapper.selectPage(pageParam, wrapper);
    }

    /**
     * 删除文件记录+物理删除文件
     */
    public boolean deleteFile(Long fileId) {
        FileRecord record = fileRecordMapper.selectById(fileId);
        if (record == null) {
            throw new RuntimeException("文件不存在");
        }

        // 物理删除文件
        Path fullPath = Paths.get(record.getFilePath()).isAbsolute()
                ? Paths.get(record.getFilePath())
                : Paths.get(storageConfig.getPath()).resolveSibling(record.getFilePath());
        try {
            Files.deleteIfExists(fullPath);
        } catch (IOException e) {
            log.warn("物理删除文件失败: {}, 继续删除数据库记录", fullPath, e);
        }

        // 删除数据库记录
        return fileRecordMapper.deleteById(fileId) > 0;
    }

    /**
     * 获取某用户上传的文件列表
     */
    public List<FileRecord> getFilesByUploader(Long uploaderId) {
        LambdaQueryWrapper<FileRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileRecord::getUploaderId, uploaderId)
               .orderByDesc(FileRecord::getCreateTime);
        return fileRecordMapper.selectList(wrapper);
    }

    /**
     * 根据MIME类型判断文件类型
     * 1-图片, 2-文档, 3-视频, 4-音频, 5-其他
     */
    private int determineFileType(String contentType) {
        if (contentType == null) {
            return 5;
        }
        if (contentType.startsWith("image/")) {
            return 1;
        } else if (contentType.startsWith("video/")) {
            return 3;
        } else if (contentType.startsWith("audio/")) {
            return 4;
        } else if (contentType.equals("application/pdf")
                || contentType.startsWith("application/msword")
                || contentType.startsWith("application/vnd.openxmlformats-officedocument")
                || contentType.equals("text/plain")) {
            return 2;
        }
        return 5;
    }
}

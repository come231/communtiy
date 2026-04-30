package com.community.file.service;



import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.community.common.dto.FileInfoDTO;
import com.community.common.util.SnowflakeIdWorker;

import com.community.file.config.OssProperties;

import com.community.file.config.StorageConfig;

import com.community.file.entity.FileRecord;

import com.community.file.mapper.FileRecordMapper;

import com.community.file.storage.AliyunOssStorage;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.springframework.util.StringUtils;

import org.springframework.web.multipart.MultipartFile;



import java.io.IOException;

import java.io.InputStream;

import java.nio.file.Files;

import java.nio.file.Path;

import java.nio.file.Paths;

import java.nio.file.StandardCopyOption;

import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;



@Slf4j

@Service

@RequiredArgsConstructor(onConstructor_ = @Autowired)

public class FileService {



    private static final long LOCAL_MAX_FILE_SIZE = 10L * 1024 * 1024;

    private static final long OSS_DEFAULT_MAX = 100L * 1024 * 1024;

    public static final String OSS_STORED_PREFIX = "oss:";



    private final FileRecordMapper fileRecordMapper;

    private final StorageConfig storageConfig;

    private final AliyunOssStorage aliyunOssStorage;

    private final OssProperties ossProperties;



    public boolean isOssRecord(FileRecord record) {

        return record != null

                && StringUtils.hasText(record.getStoredName())

                && record.getStoredName().startsWith(OSS_STORED_PREFIX);

    }



    public String ossObjectKey(FileRecord record) {

        if (!isOssRecord(record)) {

            return null;

        }

        return record.getStoredName().substring(OSS_STORED_PREFIX.length());

    }



    /**

     * 供前端拼接：本地走网关，OSS 落库时已是公网直链

     */

    public String buildFileAccessUrl(Long fileId) {

        String prefix = storageConfig.getPublicUrlPrefix();

        if (StringUtils.hasText(prefix)) {

            String p = prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;

            return p + "/file/download/" + fileId;

        }

        return "/api/file/download/" + fileId;

    }



    private long maxUploadBytes() {

        if (isOss()) {

            if (ossProperties.getMaxFileSize() != null) {

                return ossProperties.getMaxFileSize().toBytes();

            }

            return OSS_DEFAULT_MAX;

        }

        return LOCAL_MAX_FILE_SIZE;

    }



    private boolean isOss() {

        return "oss".equalsIgnoreCase(StringUtils.hasText(storageConfig.getStorage()) ? storageConfig.getStorage() : "local");

    }



    public FileRecord uploadFile(MultipartFile file, Long uploaderId) {
        return uploadFile(file, uploaderId, "file", false);
    }

    /**
     * @param biz OSS 对象键前缀
     * @param avatarUserIdObjectKey 为 true 时对象键为 {@code {biz}/{userId}{ext}}，同用户会删旧并覆盖，需已登录
     */
    public FileRecord uploadFile(MultipartFile file, Long uploaderId, String biz, boolean avatarUserIdObjectKey) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }
        if (file.getSize() > maxUploadBytes()) {
            long mb = maxUploadBytes() / (1024 * 1024);
            throw new RuntimeException("文件大小不能超过" + (mb < 1 ? 1 : mb) + "MB");
        }
        if (isOss()) {
            if (avatarUserIdObjectKey) {
                if (uploaderId == null || uploaderId <= 0) {
                    throw new RuntimeException("头像上传需指定登录用户");
                }
                return uploadToOssAvatarKey(file, uploaderId, normalizeBiz(biz));
            }
            return uploadToOss(file, uploaderId, normalizeBiz(biz));
        }
        return uploadToLocal(file, uploaderId);
    }

    public FileRecord uploadFile(MultipartFile file, Long uploaderId, String biz) {
        return uploadFile(file, uploaderId, biz, false);
    }

    public int getPresignExpireSeconds() {
        int s = ossProperties.getPresignExpireSeconds();
        return s > 0 ? s : 3600;
    }

    /**
     * OSS 访问：私有读时生成预签名，公开读时返回已存公网或拼接公网
     */
    public String getOssHttpGetUrl(FileRecord record) {
        if (!isOssRecord(record)) {
            return null;
        }
        String key = ossObjectKey(record);
        if (ossProperties.isPrivateRead()) {
            return aliyunOssStorage.generatePresignedGetUrl(key, getPresignExpireSeconds());
        }
        if (StringUtils.hasText(record.getUrl())) {
            return record.getUrl();
        }
        return aliyunOssStorage.publicUrl(key);
    }



    /**
     * 将 biz 规范为安全的对象键前缀，支持多级目录（如 {@code community/images/avatar}）。
     * 历史实现把斜杠也换成下划线，导致无法按目录归档。
     */
    private String normalizeBiz(String biz) {
        if (!StringUtils.hasText(biz)) {
            return "file";
        }
        String s = biz.trim().replace('\\', '/');
        while (s.contains("//")) {
            s = s.replace("//", "/");
        }
        s = s.toLowerCase();
        if (s.startsWith("/")) {
            s = s.substring(1);
        }
        if (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        if (!StringUtils.hasText(s)) {
            return "file";
        }
        String[] parts = s.split("/");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty() || ".".equals(p) || "..".equals(p)) {
                throw new RuntimeException("非法目录: " + biz);
            }
            if (!p.matches("[a-z0-9][a-z0-9_\\-]*")) {
                throw new RuntimeException("目录段仅允许小写字母、数字、下划线、短横: " + p);
            }
            if (sb.length() > 0) {
                sb.append('/');
            }
            sb.append(p);
        }
        return sb.isEmpty() ? "file" : sb.toString();
    }



    private FileRecord uploadToOss(MultipartFile file, Long uploaderId, String biz) {

        String contentType = file.getContentType();

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unnamed";

        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MMdd"));

        String ext = extOf(originalName);

        String uuid = UUID.randomUUID().toString().replace("-", "");

        String objectKey = biz + "/" + datePath + "/" + uuid + ext;

        String stored = OSS_STORED_PREFIX + objectKey;



        try (InputStream in = file.getInputStream()) {

            aliyunOssStorage.putObject(objectKey, in, file.getSize(), contentType != null ? contentType : "application/octet-stream");

        } catch (IOException e) {

            log.error("OSS 上传失败: {}", objectKey, e);

            throw new RuntimeException("OSS 上传失败: " + e.getMessage());

        }



        FileRecord record = new FileRecord();

        record.setId(SnowflakeIdWorker.nextId());

        record.setUserId(uploaderId != null ? uploaderId : 0L);

        record.setOriginalName(originalName);

        record.setStoredName(stored);

        // 私有读：不存公网直链；须写非 null，否则 MyBatis-Plus 省略列且表 url NOT NULL 会插入失败
        if (ossProperties.isPrivateRead()) {
            record.setUrl("");
        } else {
            record.setUrl(aliyunOssStorage.publicUrl(objectKey));
        }

        record.setSize(file.getSize());

        record.setMimeType(contentType != null ? contentType : "application/octet-stream");

        record.setCreateTime(LocalDateTime.now());

        fileRecordMapper.insert(record);

        log.info("OSS 文件上传成功: {}, fileId: {}", objectKey, record.getId());

        return record;

    }

    /**
     * 头像：对象键 {@code {biz}/{userId}{ext}}，覆盖同用户旧文件，避免 OSS 堆积
     */
    private FileRecord uploadToOssAvatarKey(MultipartFile file, Long uploaderId, String bizPrefix) {
        String contentType = file.getContentType();
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "avatar";
        String ext = extOf(originalName).toLowerCase();
        if (!ext.matches("\\.(jpg|jpeg|png|gif|webp|bmp)")) {
            throw new RuntimeException("头像仅支持 jpg/png/gif/webp/bmp");
        }
        String objectKey = bizPrefix + "/" + uploaderId + ext;
        String stored = OSS_STORED_PREFIX + objectKey;
        removePreviousAvatarObjects(uploaderId, bizPrefix);
        try (InputStream in = file.getInputStream()) {
            aliyunOssStorage.putObject(objectKey, in, file.getSize(),
                    contentType != null ? contentType : "application/octet-stream");
        } catch (IOException e) {
            log.error("OSS 头像上传失败: {}", objectKey, e);
            throw new RuntimeException("OSS 上传失败: " + e.getMessage());
        }
        FileRecord record = new FileRecord();
        record.setId(SnowflakeIdWorker.nextId());
        record.setUserId(uploaderId);
        record.setOriginalName(originalName);
        record.setStoredName(stored);
        if (ossProperties.isPrivateRead()) {
            record.setUrl("");
        } else {
            record.setUrl(aliyunOssStorage.publicUrl(objectKey));
        }
        record.setSize(file.getSize());
        record.setMimeType(contentType != null ? contentType : "application/octet-stream");
        record.setCreateTime(LocalDateTime.now());
        fileRecordMapper.insert(record);
        log.info("OSS 头像上传成功(覆盖): {}, fileId: {}", objectKey, record.getId());
        return record;
    }

    private void removePreviousAvatarObjects(Long userId, String bizPrefix) {
        String like = "oss:" + bizPrefix + "/" + userId;
        var old = fileRecordMapper.selectList(
                new LambdaQueryWrapper<FileRecord>()
                        .eq(FileRecord::getUserId, userId)
                        .likeRight(FileRecord::getStoredName, like));
        for (FileRecord r : old) {
            if (isOssRecord(r)) {
                try {
                    aliyunOssStorage.deleteObject(ossObjectKey(r));
                } catch (Exception e) {
                    log.warn("删除旧头像 OSS 对象失败: {}", e.getMessage());
                }
            }
            fileRecordMapper.deleteById(r.getId());
        }
    }

    private String extOf(String name) {

        int i = name.lastIndexOf('.');

        if (i < 0 || i == name.length() - 1) {

            return ".bin";

        }

        return name.substring(i);

    }



    private FileRecord uploadToLocal(MultipartFile file, Long uploaderId) {

        String contentType = file.getContentType();

        String originalName = file.getOriginalFilename();

        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MMdd"));

        String uuid = UUID.randomUUID().toString().replace("-", "");

        String fileName = uuid + "_" + (originalName != null ? originalName : "file");

        String relativePath = "uploads/" + datePath + "/" + fileName;



        Path fullPath = resolveUploadPath(datePath, fileName);



        try {

            Path parent = fullPath.getParent();

            if (parent != null) {

                Files.createDirectories(parent);

            }

            try (InputStream in = file.getInputStream()) {

                Files.copy(in, fullPath, StandardCopyOption.REPLACE_EXISTING);

            }

        } catch (IOException e) {

            log.error("文件保存失败: {}", fullPath, e);

            throw new RuntimeException("文件保存失败: " + e.getMessage());

        }



        FileRecord record = new FileRecord();

        record.setId(SnowflakeIdWorker.nextId());

        record.setUserId(uploaderId != null ? uploaderId : 0L);

        record.setOriginalName(originalName != null ? originalName : "");

        record.setStoredName(relativePath);

        record.setUrl(buildFileAccessUrl(record.getId()));

        record.setSize(file.getSize());

        record.setMimeType(contentType != null ? contentType : "application/octet-stream");

        record.setCreateTime(LocalDateTime.now());



        fileRecordMapper.insert(record);

        log.info("文件上传成功: {}, fileId: {}", originalName, record.getId());

        return record;

    }



    private Path storageRoot() {

        return Paths.get(storageConfig.getPath()).toAbsolutePath().normalize();

    }



    private Path resolveUploadPath(String datePath, String fileName) {

        Path dir = storageRoot();

        for (String part : datePath.split("[/\\\\]+")) {

            if (!part.isEmpty()) {

                dir = dir.resolve(part);

            }

        }

        return dir.resolve(fileName).normalize();

    }



    private Path resolveStoredFile(String relativePath) {

        if (relativePath == null || relativePath.isEmpty()) {

            throw new RuntimeException("文件路径无效");

        }

        String rp = relativePath.replace('\\', '/');

        Path parentOfUploads = storageRoot().getParent();

        if (parentOfUploads == null) {

            parentOfUploads = Paths.get(".").toAbsolutePath().normalize();

        }

        return parentOfUploads.resolve(rp).normalize();

    }



    public byte[] downloadFile(Long fileId) {

        FileRecord record = getFileInfo(fileId);

        if (isOssRecord(record)) {

            String key = ossObjectKey(record);

            try {

                return aliyunOssStorage.getObjectBytes(key);

            } catch (IOException e) {

                log.error("OSS 读取失败: {}", key, e);

                throw new RuntimeException("文件读取失败: " + e.getMessage());

            }

        }

        String pathKey = record.getStoredName();

        Path fullPath = Paths.get(pathKey).isAbsolute()

                ? Paths.get(pathKey).normalize()

                : resolveStoredFile(pathKey);

        try {

            return Files.readAllBytes(fullPath);

        } catch (IOException e) {

            log.error("文件读取失败: {}", fileId, e);

            throw new RuntimeException("文件读取失败: " + e.getMessage());

        }

    }



    public FileRecord getFileInfo(Long fileId) {

        FileRecord record = fileRecordMapper.selectById(fileId);

        if (record == null) {

            throw new RuntimeException("文件不存在");

        }

        return record;

    }



    public Page<FileRecord> getFileList(int page, int size) {

        Page<FileRecord> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<FileRecord> wrapper = new LambdaQueryWrapper<>();

        wrapper.orderByDesc(FileRecord::getCreateTime);

        return fileRecordMapper.selectPage(pageParam, wrapper);

    }



    public boolean deleteFile(Long fileId) {

        FileRecord record = fileRecordMapper.selectById(fileId);

        if (record == null) {

            throw new RuntimeException("文件不存在");

        }

        if (isOssRecord(record)) {

            String key = ossObjectKey(record);

            try {

                aliyunOssStorage.deleteObject(key);

            } catch (Exception e) {

                log.warn("OSS 物理删除失败: {}，仍删除元数据: {}", key, e.getMessage());

            }

        } else {

            String pathKey = record.getStoredName();

            Path fullPath = Paths.get(pathKey).isAbsolute()

                    ? Paths.get(pathKey).normalize()

                    : resolveStoredFile(pathKey);

            try {

                Files.deleteIfExists(fullPath);

            } catch (IOException e) {

                log.warn("物理删除文件失败: {}, 继续删除数据库记录", fullPath, e);

            }

        }

        return fileRecordMapper.deleteById(fileId) > 0;

    }

    /**
     * 删除「创建时间早于 minAgeHours」且 id 不在 referencedIds 中的文件（OSS + 元数据）。
     * 用于：编辑仅改库、旧图旧视频不删；由定时任务按引用表收敛后再物理删除无引用资源。
     */
    public int deleteOrphanFilesNotReferenced(Set<Long> referencedIds, int minAgeHours) {
        if (minAgeHours < 1) {
            minAgeHours = 24;
        }
        Set<Long> ref = referencedIds != null ? referencedIds : new HashSet<>();
        LocalDateTime cutoff = LocalDateTime.now().minusHours(minAgeHours);
        int deleted = 0;
        int pageNo = 1;
        final int size = 400;
        while (true) {
            Page<FileRecord> p = new Page<>(pageNo, size);
            LambdaQueryWrapper<FileRecord> w = new LambdaQueryWrapper<>();
            w.lt(FileRecord::getCreateTime, cutoff);
            w.orderByAsc(FileRecord::getId);
            Page<FileRecord> res = fileRecordMapper.selectPage(p, w);
            if (res.getRecords().isEmpty()) {
                break;
            }
            for (FileRecord r : res.getRecords()) {
                if (r.getId() == null || ref.contains(r.getId())) {
                    continue;
                }
                try {
                    deleteFile(r.getId());
                    deleted++;
                } catch (Exception e) {
                    log.warn("GC 删除 fileId={} 失败: {}", r.getId(), e.getMessage());
                }
            }
            if (res.getRecords().size() < size) {
                break;
            }
            pageNo++;
        }
        return deleted;
    }

    public List<FileRecord> getFilesByUploader(Long uploaderId) {

        LambdaQueryWrapper<FileRecord> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(FileRecord::getUserId, uploaderId)

               .orderByDesc(FileRecord::getCreateTime);

        return fileRecordMapper.selectList(wrapper);

    }

    /**
     * 供其他微服务按 ID 批量取元信息（做体积校验、鉴权等）
     */
    public List<FileInfoDTO> listFileInfoByIds(List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return List.of();
        }
        return fileRecordMapper.selectBatchIds(fileIds).stream()
                .map(this::toFileInfo)
                .toList();
    }

    public FileInfoDTO toFileInfo(FileRecord r) {
        if (r == null) {
            return null;
        }
        FileInfoDTO d = new FileInfoDTO();
        d.setId(r.getId() == null ? null : String.valueOf(r.getId()));
        d.setUserId(r.getUserId());
        d.setOriginalName(r.getOriginalName());
        d.setSize(r.getSize());
        d.setMimeType(r.getMimeType());
        if (ossProperties.isPrivateRead() && isOssRecord(r)) {
            d.setUrl(null);
            d.setSignedUrl(aliyunOssStorage.generatePresignedGetUrl(ossObjectKey(r), getPresignExpireSeconds()));
        } else {
            d.setUrl(r.getUrl());
            d.setSignedUrl(null);
        }
        return d;
    }
}


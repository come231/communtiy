package com.community.file.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.community.file.config.OssProperties;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

/**
 * 封装 OSS 上传、读取、删除与公网 URL 拼接
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AliyunOssStorage {

    private final OssProperties properties;
    private volatile OSS client;

    private String normalizeEndpoint() {
        String ep = properties.getEndpoint() != null ? properties.getEndpoint().trim() : "";
        if (ep.isEmpty()) {
            throw new IllegalStateException("aliyun.oss.endpoint 未配置");
        }
        if (ep.startsWith("http://") || ep.startsWith("https://")) {
            return ep;
        }
        return "https://" + ep;
    }

    public OSS getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    if (!StringUtils.hasText(properties.getAccessKeyId()) || !StringUtils.hasText(properties.getAccessKeySecret())) {
                        throw new IllegalStateException("aliyun.oss.access-key-id 或 access-key-secret 未配置");
                    }
                    if (!StringUtils.hasText(properties.getBucketName())) {
                        throw new IllegalStateException("aliyun.oss.bucket-name 未配置");
                    }
                    client = new OSSClientBuilder().build(
                            normalizeEndpoint(),
                            properties.getAccessKeyId().trim(),
                            properties.getAccessKeySecret().trim()
                    );
                }
            }
        }
        return client;
    }

    @PreDestroy
    public void shutdown() {
        if (client != null) {
            try {
                client.shutdown();
            } catch (Exception e) {
                log.warn("OSS 客户端关闭异常: {}", e.getMessage());
            }
        }
    }

    public void putObject(String objectKey, InputStream in, long contentLength, String contentType) {
        com.aliyun.oss.model.ObjectMetadata meta = new com.aliyun.oss.model.ObjectMetadata();
        meta.setContentLength(contentLength);
        if (StringUtils.hasText(contentType)) {
            meta.setContentType(contentType);
        }
        getClient().putObject(properties.getBucketName(), objectKey, in, meta);
    }

    public byte[] getObjectBytes(String objectKey) throws IOException {
        OSSObject object = getClient().getObject(properties.getBucketName(), objectKey);
        try (InputStream in = object.getObjectContent()) {
            return in.readAllBytes();
        }
    }

    public void deleteObject(String objectKey) {
        getClient().deleteObject(properties.getBucketName(), objectKey);
    }

    /**
     * 私有 Bucket 时用于浏览器/302 直链的临时只读地址
     */
    public String generatePresignedGetUrl(String objectKey, int expireSeconds) {
        Date expiration = new Date(System.currentTimeMillis() + (long) expireSeconds * 1000L);
        URL u = getClient().generatePresignedUrl(properties.getBucketName(), objectKey, expiration);
        return u.toString();
    }

    /**
     * 与控制台「默认域名」或自定义域名一致的可访问地址
     */
    public String publicUrl(String objectKey) {
        if (!StringUtils.hasText(properties.getBaseUrl())) {
            // 无自定义域名时： virtual-hosted style
            String ep = properties.getEndpoint().replace("https://", "").replace("http://", "");
            return "https://" + properties.getBucketName() + "." + ep + "/" + objectKey;
        }
        String base = properties.getBaseUrl().trim();
        if (base.endsWith("/")) {
            return base + objectKey;
        }
        return base + "/" + objectKey;
    }
}

package com.community.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

/**
 * 与 application.yml 中 aliyun.oss 段对应
 */
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssProperties {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    /**
     * 公共读时用于拼接直链，如 https://xxx.oss-cn-beijing.aliyuncs.com/ （末尾可带/）
     */
    private String baseUrl;
    /**
     * 如 100MB，未配置时由 {@link com.community.file.service.FileService} 回退
     */
    private DataSize maxFileSize;
    /**
     * true=私有读，不存公网直链，访问走预签名；false=与旧版一致写死公网 url 并 302
     */
    private boolean privateRead = true;
    /**
     * 预签名 GET 有效期（秒），默认 1 小时
     */
    private int presignExpireSeconds = 3600;
}

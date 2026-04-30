package com.community.file.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "upload")
public class StorageConfig {

    /**
     * local=本地磁盘，oss=阿里云 OSS（与 aliyun.oss 联用）
     */
    private String storage = "local";

    private String path = "./uploads/";
    /**
     * 公网访问文件服务前缀（如 http://112.124.108.191:8080/api），不配则返回 /api/file/download/{id}；oss 时仍可用于兼容旧直链
     */
    private String publicUrlPrefix = "";

    @PostConstruct
    public void init() {
        if (!"oss".equalsIgnoreCase(storage)) {
            File uploadDir = new File(path);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
        }
    }
}

package com.community.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 无引用文件清理，仅当配置了 internal-token 时允许 /file/internal/gc/* 被调用
 */
@Data
@Component
@ConfigurationProperties(prefix = "community.file.gc")
public class FileGcProperties {

    private String internalToken;
}

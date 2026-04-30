package com.community.common.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 与文件服务 /file/infos 返回项对应，供 Feign 反序列化
 */
@Data
public class FileInfoDTO implements Serializable {
    /**
     * 使用字符串，避免在 JSON/JS 中超过 Number.MAX_SAFE_INTEGER 时失准
     */
    private String id;
    private Long userId;
    private String originalName;
    private String url;
    /**
     * 私有读 Bucket 时短期 GET 预签名，供一次性展示
     */
    private String signedUrl;
    private Long size;
    private String mimeType;
}

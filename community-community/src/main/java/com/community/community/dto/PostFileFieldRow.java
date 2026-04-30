package com.community.community.dto;

import lombok.Data;

/**
 * GC 引用扫描用：与 {@code post} 中 URL/JSON 字段一致；另见 {@code listCoverFileIds} 的数值封面列
 */
@Data
public class PostFileFieldRow {
    private String images;
    private String coverImage;
    private String video;
    private String content;
}

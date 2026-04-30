package com.community.community.dto;

import lombok.Data;

/**
 * 与 {@code post_media} 对应，用于发帖/改帖时整帖（图+视）与 100MB 总上限校验
 */
@Data
public class PostMediaItemDTO {
    /**
     * file_record.id，JSON 用字符串，避免 JavaScript 双精度大整数失真
     */
    private String fileId;
    /** 1=image 2=video */
    private Integer mediaType;
    /** 0 起，显示顺序 */
    private Integer sortOrder;
}

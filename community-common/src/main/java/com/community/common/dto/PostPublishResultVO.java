package com.community.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 发帖接口返回：明确「已公开发布」或「待人工审核」，便于前台即时展示。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostPublishResultVO implements Serializable {

    private Long postId;
    /**
     * {@link com.community.common.constant.CommonConstant#POST_STATUS_NORMAL} 已发布 /
     * {@link com.community.common.constant.CommonConstant#POST_STATUS_REVIEWING} 待人工审核
     */
    private Integer status;
    /** 给用户看的说明文案 */
    private String message;
}

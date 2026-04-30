package com.community.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子导出为文档；业务上转文档时 exclude 视频
 */
@Data
@TableName("post_document_export")
public class PostDocumentExport {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long postId;
    private Long userId;
    private Long fileId;
    private String format;
    /**
     * 转文档为 0（不含视频）
     */
    private Integer includeVideo;
    /**
     * 0=处理中 1=成功 2=失败
     */
    private Integer status;
    private String errorMessage;
    private String metaJson;
    private LocalDateTime createTime;
    private LocalDateTime finishTime;
}

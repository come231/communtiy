package com.community.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子多图/多视频，与 {@link com.community.community.entity.Post#totalMediaBytes} 对账
 */
@Data
@TableName("post_media")
public class PostMedia {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long postId;
    private Long fileId;
    /**
     * 1=image 2=video
     */
    private Integer mediaType;
    private Long sizeBytes;
    @TableField("sort_order")
    private Integer sortOrder;
    private Integer width;
    private Integer height;
    private Integer durationMs;
    private LocalDateTime createTime;
}

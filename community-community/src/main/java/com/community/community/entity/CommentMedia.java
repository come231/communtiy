package com.community.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论中自定义图片/表情等资源
 */
@Data
@TableName("comment_media")
public class CommentMedia {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long commentId;
    private Long fileId;
    /**
     * 1=贴纸或配图 2=其他
     */
    private Integer mediaKind;
    @TableField("sort_order")
    private Integer sortOrder;
    private Long sizeBytes;
    private Integer width;
    private Integer height;
    private LocalDateTime createTime;
}

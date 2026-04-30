package com.community.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {
    @TableId(value = "comment_id", type = IdType.AUTO)
    private Long commentId;
    private Long postId;
    private Long userId;
    private String content;
    /**
     * 0=纯文本 1=含自定义图/表情
     */
    private Integer contentMode;
    private Long parentId;
    private Long replyUid;
    private Integer likeCount;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private Integer floor;
}

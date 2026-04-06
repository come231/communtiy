package com.community.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("post_draft")
public class PostDraft {
    @TableId(value = "draft_id", type = IdType.AUTO)
    private Long draftId;
    private Long userId;
    private Long sectionId;
    private String title;
    private String content;
    private String images;
    private String video;
    private String topicIds;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

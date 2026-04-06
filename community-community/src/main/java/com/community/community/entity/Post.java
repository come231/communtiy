package com.community.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("post")
public class Post {
    @TableId(value = "post_id", type = IdType.AUTO)
    private Long postId;
    private Long userId;
    private Long sectionId;
    private String title;
    private String content;
    private String images;
    private String video;
    private Long voteId;
    private Long columnId;
    private Integer isAnonymous;
    private String summary;
    private String keywords;
    private String topicIds;
    private Integer isTop;
    private Integer isEssence;
    private Long viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer collectCount;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    private Integer shareCount;
    private String tags;
}

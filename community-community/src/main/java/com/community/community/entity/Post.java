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
    private String coverImage;
    private String video;
    /**
     * 本帖已关联媒体总大小（字节），与 post_media 汇总一致；业务上限 100MB
     */
    private Long totalMediaBytes;
    /**
     * 可选，封面 file_record.id
     */
    private Long coverFileId;
    private Long voteId;
    private Long columnId;
    private Integer isAnonymous;
    /** 是否公开: 1公开 0私密(仅自己可见) */
    private Integer isPublic;
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
    /** 审核打回时管理员填写的原因，仅作者端展示 */
    private String rejectReason;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    private Integer shareCount;
    private String tags;
}

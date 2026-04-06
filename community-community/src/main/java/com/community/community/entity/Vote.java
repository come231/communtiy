package com.community.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("vote")
public class Vote {
    @TableId(value = "vote_id", type = IdType.AUTO)
    private Long voteId;
    private Long userId;
    private Long postId;
    private String title;
    private String description;
    private String image;
    private Integer voteType;
    private Integer isAnonymous;
    private Integer isMulti;
    private Integer maxSelect;
    private LocalDateTime expireTime;
    private Integer totalVotes;
    private Integer viewCount;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

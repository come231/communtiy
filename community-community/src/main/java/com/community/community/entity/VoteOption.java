package com.community.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("vote_option")
public class VoteOption {
    @TableId(value = "option_id", type = IdType.AUTO)
    private Long optionId;
    private Long voteId;
    private String content;
    private String image;
    private Integer voteCount;
    private Integer sort;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

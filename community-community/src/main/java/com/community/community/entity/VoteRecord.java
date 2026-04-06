package com.community.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("vote_record")
public class VoteRecord {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long voteId;
    private Long optionId;
    private Long userId;
    private String ip;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

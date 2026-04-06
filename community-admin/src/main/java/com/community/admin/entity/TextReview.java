package com.community.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("text_review")
public class TextReview {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Integer contentId;
    private Integer contentType;
    private String originalContent;
    private Integer checkResult;
    private String sensitiveWords;
    private LocalDateTime createTime;
}

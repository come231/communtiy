package com.community.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sensitive_word")
public class SensitiveWord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String word;
    private Integer type;
    private Integer level;
    private String replaceWord;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

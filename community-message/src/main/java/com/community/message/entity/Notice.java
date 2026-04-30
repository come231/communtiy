package com.community.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notice")
public class Notice {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;


    private String content;

    private Integer type;

    @TableField("is_read")
    private Boolean isRead;

    private Long fromUid;
    private Long targetId;

    private String extraData;

    private LocalDateTime createTime;

    public Notice() {
        this.isRead = false;
        this.createTime = LocalDateTime.now();
    }
}
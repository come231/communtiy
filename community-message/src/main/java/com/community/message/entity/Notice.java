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

    // 🔥 表没有，删掉
    // private String title;

    private String content;

    private Integer type;

    @TableField("is_read")
    private Boolean isRead;

    // 🔥 表没有这两个，删掉
    // private Long relatedId;
    // private Integer relatedType;

    // 🔥 表真实存在的字段，加上！
    private Long fromUid;
    private Long targetId;

    private String extraData;

    private LocalDateTime createTime;

    public Notice() {
        this.isRead = false;
        this.createTime = LocalDateTime.now();
    }
}
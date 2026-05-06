package com.community.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("community_security.sensitive_filter_record")
public class SensitiveFilterRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long commentId;
    private Long userId;
    private String username;
    private String originalContent;
    private String filteredContent;
    private String hitWords;
    private LocalDateTime createTime;
}

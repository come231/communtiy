package com.community.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SensitiveFilterRecordVO {
    private Long id;
    private Long commentId;
    private Long userId;
    private String username;
    private String originalContent;
    private String filteredContent;
    private List<String> hitWords;
    private LocalDateTime createTime;
}

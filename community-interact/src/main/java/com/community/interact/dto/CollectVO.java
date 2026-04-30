package com.community.interact.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CollectVO {

    private Long id;
    private Long userId;
    private Long postId;
    private Long folderId;
    private String postTitle;
    private String postSummary;
    private LocalDateTime createTime;
    private Integer isHidden;
}
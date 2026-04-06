package com.community.search.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PostSyncMessage implements Serializable {

    private Long id;
    private String title;
    private String content;
    private String summary;
    private Long authorId;
    private String authorName;
    private Long sectionId;
    private String sectionName;
    private Integer status;
    private LocalDateTime createTime;
    private Integer viewCount;
    private Integer likeCount;

    /**
     * 操作类型: CREATE, UPDATE, DELETE
     */
    private String action;
}

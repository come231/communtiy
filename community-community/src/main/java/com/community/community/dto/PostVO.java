package com.community.community.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PostVO {
    private Long postId;
    private Long userId;
    private Long authorId;  // 作者ID，等同于userId
    private Long sectionId;
    private String sectionName;
    private String title;
    private String content;
    private String images;
    private String summary;
    private String tags;
    private Integer isTop;
    private Integer isEssence;
    private Integer isAnonymous;
    private Long viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer collectCount;
    private Integer shareCount;
    private LocalDateTime createTime;
    private String authorName;
    private String authorAvatar;
    private Boolean isLiked;
    private Boolean isCollected;
}

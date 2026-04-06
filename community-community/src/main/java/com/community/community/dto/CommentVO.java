package com.community.community.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {
    private Long commentId;
    private Long postId;
    private Long userId;
    private String content;
    private Long parentId;
    private Long replyUid;
    private Integer likeCount;
    private Integer floor;
    private LocalDateTime createTime;
    private String authorName;
    private String authorAvatar;
    private String replyUserName;
    private Boolean isLiked;
    private List<CommentVO> replies;
}

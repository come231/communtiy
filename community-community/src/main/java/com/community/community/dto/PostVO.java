package com.community.community.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

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
    private String coverImage;
    private Long coverFileId;
    /** 与 images 中首批视频或 post.video 一致，主视频下载路径或外链 */
    private String video;
    private Long totalMediaBytes;
    private List<PostMediaVO> media;
    private String summary;
    private String tags;
    private Integer isTop;
    private Integer isEssence;
    private Integer isAnonymous;
    /** 是否公开: 1公开 0私密 */
    private Integer isPublic;
    private Long viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer collectCount;
    private Integer status;
    /** 违规打回时的说明（若有） */
    private String rejectReason;
    private Integer shareCount;
    private LocalDateTime createTime;
    private String authorName;
    private String authorAvatar;
    private Boolean isLiked;
    private Boolean isCollected;

    /** 与 postId 相同，供管理端等使用字段 id 的协议 */
    public Long getId() {
        return postId;
    }
}

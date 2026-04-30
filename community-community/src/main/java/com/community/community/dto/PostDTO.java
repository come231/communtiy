package com.community.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class PostDTO {
    /** 可选；未传时使用系统默认板块（排序第一且启用） */
    private Long sectionId;
    @NotBlank(message = "标题不能为空")
    private String title;
    @NotBlank(message = "内容不能为空")
    private String content;
    private String images;
    /** 封面图 URL，用于列表/详情页展示 */
    private String coverImage;
    /** 可选，与 coverImage 二选一；用字符串可完整传递雪花 file_record.id */
    private String coverFileId;
    private String video;
    /**
     * 非 null 时：完全替换本帖的 post_media，并回写 images/video/totalMediaBytes；空列表表示清空图库
     * 为 null 时保持旧表数据（仅改标题正文等未传该字段的客户端）
     */
    private List<PostMediaItemDTO> media;
    private List<Long> topicIds;
    private Boolean isAnonymous;
    /** 是否公开: true公开(默认) false私密(仅自己可见) */
    private Boolean isPublic;
    private String tags;
}

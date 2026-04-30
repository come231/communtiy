package com.community.community.dto;

import lombok.Data;

import java.util.List;

/**
 * 草稿保存：字段全部可选，避免未填标题/正文时无法落库。
 */
@Data
public class PostDraftSaveDTO {
    /** 传入则更新该草稿，否则新建 */
    private Long draftId;
    private String title;
    private String content;
    private Long sectionId;
    private String images;
    private String video;
    private String coverImage;
    private List<Long> topicIds;
    /** 草稿中记录的匿名偏好 */
    private Boolean isAnonymous;
}

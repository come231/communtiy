package com.community.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PostDTO {
    @NotNull(message = "板块ID不能为空")
    private Long sectionId;
    @NotBlank(message = "标题不能为空")
    private String title;
    @NotBlank(message = "内容不能为空")
    private String content;
    private String images;
    private String video;
    private String topicIds;
    private Integer isAnonymous;
    private String tags;
}

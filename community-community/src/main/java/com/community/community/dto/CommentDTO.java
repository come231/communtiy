package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CommentDTO {
    @NotNull(message = "帖子ID不能为空")
    private Long postId;
    /**
     * 纯文字；可与 imageFileIds 二选一，至少一种非空由服务端校验
     */
    private String content;
    private Long parentId;
    /** 与前端 commentApi.create 字段 replyToUserId 对齐 */
    @JsonProperty("replyToUserId")
    private Long replyUid;
    /** 评论配图，file_record.id 列表（JSON 用字符串，避免大整数丢失） */
    private List<String> imageFileIds;
}

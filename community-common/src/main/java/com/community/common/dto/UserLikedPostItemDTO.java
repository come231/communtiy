package com.community.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户点赞的帖子条目（互动服务分页返回，供用户服务等消费）
 */
@Data
public class UserLikedPostItemDTO implements Serializable {

    private Long postId;
    private LocalDateTime likeTime;
}

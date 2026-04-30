package com.community.user.dto;

import lombok.Data;

@Data
public class FollowUserVO {
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private Boolean isFollowed;
}
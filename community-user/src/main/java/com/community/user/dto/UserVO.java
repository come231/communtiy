package com.community.user.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class UserVO {

    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String background;
    private String phone;
    private Integer gender;
    private String signature;
    private java.time.LocalDate birthday;
    private Integer role;
    private Integer exp;
    private Integer points;
    private Integer postCount;
    private Integer collectCount;
    /** 获赞总数 */
    private Integer likeCount;
    /** 是否隐藏喜欢列表: 0公开 1隐藏 */
    private Integer hideLikes;
    /** 是否隐藏收藏列表: 0公开 1隐藏 */
    private Integer hideCollects;
    /** 个人标签，逗号分隔 */
    private String bioTags;
    private Integer continuousCheckin;
    private Long followerCount;
    private Long followingCount;
    private Boolean isFollowed;
}

package com.community.common.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserDTO implements Serializable {

    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private Integer gender;
    private String signature;
    private Integer status;
    private Integer role;
    private Integer exp;
    private Integer points;
    private Integer postCount;
    private Integer collectCount;
}

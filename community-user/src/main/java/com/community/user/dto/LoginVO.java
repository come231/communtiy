package com.community.user.dto;

import lombok.Data;

import java.util.Map;

@Data
public class LoginVO {

    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private Integer role;
}

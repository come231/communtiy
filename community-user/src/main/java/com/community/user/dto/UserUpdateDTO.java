package com.community.user.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserUpdateDTO {
    private String nickname;
    private String avatar;
    private String background;
    private Integer gender;
    private String signature;
    private LocalDate birthday;
}

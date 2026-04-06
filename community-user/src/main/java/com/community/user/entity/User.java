package com.community.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    private String username;

    private String nickname;

    private String avatar;

    private String background;

    private String phone;

    private String password;

    private String salt;

    private Integer gender;

    private String signature;

    private LocalDate birthday;

    private Integer status;

    private Integer role;

    private Long columnId;

    private Integer exp;

    private Integer points;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private LocalDateTime muteEndTime;

    private Integer postCount;

    private Integer collectCount;

    private LocalDate lastCheckin;

    private Integer continuousCheckin;
}

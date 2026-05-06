package com.community.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SensitiveWordVO {
    private Long id;
    private String word;
    private Integer level;
    private Integer type;
    private String category;
    private String replacement;
    private Integer status;
    private Boolean enabled;
    private Long hitCount;
    private Long todayHits;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

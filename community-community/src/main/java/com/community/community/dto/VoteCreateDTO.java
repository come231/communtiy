package com.community.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VoteCreateDTO {
    private Long postId;
    @NotBlank(message = "投票标题不能为空")
    private String title;
    private String description;
    private String image;
    private Integer voteType;      // 0单选 1多选
    private Integer isAnonymous;   // 0实名 1匿名
    private Integer isMulti;       // 0否 1是
    private Integer maxSelect;     // 最大可选数
    private LocalDateTime expireTime;
    private List<String> options;
}

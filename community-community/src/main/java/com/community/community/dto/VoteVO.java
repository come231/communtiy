package com.community.community.dto;

import com.community.community.entity.VoteOption;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class VoteVO {
    private Long voteId;
    private Long postId;
    private String title;
    private String description;
    private Integer voteType;
    private Integer isAnonymous;
    private Integer isMulti;
    private Integer maxSelect;
    private LocalDateTime expireTime;
    private Integer totalVotes;
    private Integer viewCount;
    private Integer status;
    private List<VoteOption> options;
    private Boolean hasVoted;
}

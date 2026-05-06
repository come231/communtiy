package com.community.admin.vo;

import lombok.Data;

@Data
public class SensitiveWordStatsVO {
    private long totalWords;
    private long totalHits;
    private long todayHits;
    private long todayNew;
}

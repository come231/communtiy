package com.community.admin.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardVO {

    private Long totalUserCount;
    private Long totalPostCount;
    private Long totalCommentCount;
    private Long todayActiveUser;
    private Long pendingReportCount;
    private List<Map<String, Object>> userTrend;
    private List<Map<String, Object>> postTrend;
    private List<Map<String, Object>> latestReports;
}

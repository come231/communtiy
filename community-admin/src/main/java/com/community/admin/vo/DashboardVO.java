package com.community.admin.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardVO {

    private Long totalUserCount;
    private Long totalPostCount;
    private Long totalCommentCount;
    private Long todayNewUsers;
    private Long todayNewPosts;
    private Long todayActiveUser;
    private Long pendingReportCount;
    
    private List<Integer> userGrowth;
    private List<Integer> postGrowth;
    private List<String> labels;
    
    private Integer userTrend;
    private Integer postTrend;
    
    private List<Map<String, Object>> userTrendData;
    private List<Map<String, Object>> postTrendData;
    private List<Map<String, Object>> latestReports;
}

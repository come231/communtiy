package com.community.admin.service;

import com.community.admin.feign.PostFeignClient;
import com.community.admin.feign.UserFeignClient;
import com.community.admin.vo.DashboardVO;
import com.community.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final JdbcTemplate jdbcTemplate;
    private final UserFeignClient userFeignClient;
    private final PostFeignClient postFeignClient;

    public DashboardVO getDashboardStats() {
        DashboardVO vo = new DashboardVO();

        try {
            // 总用户数（从用户服务获取）
            try {
                Result<Map<String, Object>> userStatsResult = userFeignClient.getUserStats();
                if (userStatsResult.isSuccess()) {
                    Map<String, Object> userStats = userStatsResult.getData();
                    vo.setTotalUserCount((Long) userStats.getOrDefault("totalUserCount", 0L));
                } else {
                    vo.setTotalUserCount(0L);
                }
            } catch (Exception e) {
                vo.setTotalUserCount(0L);
            }

            // 总帖子数（从社区服务获取）
            try {
                Result<Map<String, Object>> postStatsResult = postFeignClient.getPostStats();
                if (postStatsResult.isSuccess()) {
                    Map<String, Object> postStats = postStatsResult.getData();
                    vo.setTotalPostCount((Long) postStats.getOrDefault("totalPostCount", 0L));
                    vo.setTotalCommentCount((Long) postStats.getOrDefault("totalCommentCount", 0L));
                } else {
                    vo.setTotalPostCount(0L);
                    vo.setTotalCommentCount(0L);
                }
            } catch (Exception e) {
                vo.setTotalPostCount(0L);
                vo.setTotalCommentCount(0L);
            }

            // 今日活跃用户（从安全库查询）
            LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
            try {
                vo.setTodayActiveUser(jdbcTemplate.queryForObject(
                        "SELECT COUNT(DISTINCT user_id) FROM operation_log WHERE create_time >= ?",
                        Long.class, todayStart));
            } catch (Exception e) {
                vo.setTodayActiveUser(0L);
            }

            // 待处理举报数量
            try {
                vo.setPendingReportCount(jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM report WHERE status = 0", Long.class));
            } catch (Exception e) {
                vo.setPendingReportCount(0L);
            }
        } catch (Exception e) {
            vo.setTotalUserCount(0L);
            vo.setTotalPostCount(0L);
            vo.setTotalCommentCount(0L);
            vo.setTodayActiveUser(0L);
            vo.setPendingReportCount(0L);
            log.error("获取仪表盘统计数据异常: {}", e.getMessage(), e);
        }

        return vo;
    }

    public List<Map<String, Object>> getUserTrend(int days) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT DATE(create_time) AS date, COUNT(DISTINCT user_id) AS count " +
                    "FROM operation_log WHERE create_time >= ? GROUP BY DATE(create_time) ORDER BY date",
                    LocalDateTime.of(startDate, LocalTime.MIN));

            // 填充缺失日期
            for (int i = 0; i < days; i++) {
                LocalDate date = startDate.plusDays(i);
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", date.toString());

                boolean found = false;
                for (Map<String, Object> row : rows) {
                    java.sql.Date rowDate = (java.sql.Date) row.get("date");
                    if (rowDate != null && rowDate.toLocalDate().equals(date)) {
                        dayData.put("count", ((Number) row.get("count")).longValue());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    dayData.put("count", 0L);
                }
                result.add(dayData);
            }
        } catch (Exception e) {
            // 返回空数据
        }

        return result;
    }

    public List<Map<String, Object>> getPostTrend(int days) {
        try {
            Result<List<Map<String, Object>>> result = postFeignClient.getPostTrend(days);
            if (result != null && result.isSuccess() && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            log.warn("通过Feign获取帖子趋势失败，使用空数据: {}", e.getMessage());
        }

        // 降级：返回空数据
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            dayData.put("count", 0L);
            result.add(dayData);
        }
        return result;
    }

    public List<Map<String, Object>> getLatestReports() {
        String sql = "SELECT id, reporter_id, target_type, reason, status, create_time FROM report ORDER BY create_time DESC LIMIT 10";
        List<Map<String, Object>> reports = jdbcTemplate.queryForList(sql);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> report : reports) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", report.get("id"));
            item.put("reporterId", report.get("reporter_id"));
            item.put("targetType", report.get("target_type"));
            item.put("reason", report.get("reason"));
            item.put("status", report.get("status"));
            item.put("createTime", report.get("create_time"));
            result.add(item);
        }

        return result;
    }
}

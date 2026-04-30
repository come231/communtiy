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
                if (userStatsResult != null && userStatsResult.isSuccess() && userStatsResult.getData() != null) {
                    Map<String, Object> userStats = userStatsResult.getData();
                    vo.setTotalUserCount(((Number) userStats.getOrDefault("totalUserCount", 0L)).longValue());
                    vo.setTodayNewUsers(((Number) userStats.getOrDefault("todayNewUsers", 0L)).longValue());
                } else {
                    vo.setTotalUserCount(0L);
                    vo.setTodayNewUsers(0L);
                }
            } catch (Exception e) {
                log.warn("获取用户统计数据失败: {}", e.getMessage());
                vo.setTotalUserCount(0L);
                vo.setTodayNewUsers(0L);
            }

            // 总帖子数（从社区服务获取）
            try {
                Result<Map<String, Object>> postStatsResult = postFeignClient.getPostStats();
                if (postStatsResult != null && postStatsResult.isSuccess() && postStatsResult.getData() != null) {
                    Map<String, Object> postStats = postStatsResult.getData();
                    vo.setTotalPostCount(((Number) postStats.getOrDefault("totalPostCount", 0L)).longValue());
                    vo.setTotalCommentCount(((Number) postStats.getOrDefault("totalCommentCount", 0L)).longValue());
                    vo.setTodayNewPosts(((Number) postStats.getOrDefault("todayNewPosts", 0L)).longValue());
                } else {
                    vo.setTotalPostCount(0L);
                    vo.setTotalCommentCount(0L);
                    vo.setTodayNewPosts(0L);
                }
            } catch (Exception e) {
                log.warn("获取帖子统计数据失败: {}", e.getMessage());
                vo.setTotalPostCount(0L);
                vo.setTotalCommentCount(0L);
                vo.setTodayNewPosts(0L);
            }

            // 今日活跃用户（从安全库查询）
            LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
            try {
                vo.setTodayActiveUser(jdbcTemplate.queryForObject(
                        "SELECT COUNT(DISTINCT user_id) FROM operation_log WHERE create_time >= ?",
                        Long.class, todayStart));
            } catch (Exception e) {
                log.warn("获取今日活跃用户失败: {}", e.getMessage());
                vo.setTodayActiveUser(0L);
            }

            // 待处理举报数量
            try {
                vo.setPendingReportCount(jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM report WHERE status = 0", Long.class));
            } catch (Exception e) {
                log.warn("获取待处理举报数量失败: {}", e.getMessage());
                vo.setPendingReportCount(0L);
            }

            // 计算增长率
            try {
                List<Map<String, Object>> userTrendData = getUserTrend(7);
                List<Map<String, Object>> postTrendData = getPostTrend(7);

                if (userTrendData.size() >= 2) {
                    long todayUsers = ((Number) userTrendData.get(userTrendData.size() - 1).get("count")).longValue();
                    long yesterdayUsers = ((Number) userTrendData.get(userTrendData.size() - 2).get("count")).longValue();
                    vo.setUserTrend(yesterdayUsers > 0 ? (int) ((todayUsers - yesterdayUsers) * 100 / yesterdayUsers) : 0);
                }

                if (postTrendData.size() >= 2) {
                    long todayPosts = ((Number) postTrendData.get(postTrendData.size() - 1).get("count")).longValue();
                    long yesterdayPosts = ((Number) postTrendData.get(postTrendData.size() - 2).get("count")).longValue();
                    vo.setPostTrend(yesterdayPosts > 0 ? (int) ((todayPosts - yesterdayPosts) * 100 / yesterdayPosts) : 0);
                }

                vo.setUserGrowth(userTrendData.stream().map(m -> ((Number) m.get("count")).intValue()).toList());
                vo.setPostGrowth(postTrendData.stream().map(m -> ((Number) m.get("count")).intValue()).toList());

                List<String> labels = new ArrayList<>();
                LocalDate startDate = LocalDate.now().minusDays(6);
                for (int i = 0; i < 7; i++) {
                    labels.add(startDate.plusDays(i).getDayOfMonth() + "日");
                }
                vo.setLabels(labels);
            } catch (Exception e) {
                log.warn("计算趋势数据失败: {}", e.getMessage());
                vo.setUserTrend(0);
                vo.setPostTrend(0);
                vo.setUserGrowth(Collections.emptyList());
                vo.setPostGrowth(Collections.emptyList());
                vo.setLabels(Collections.emptyList());
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
            log.warn("获取用户趋势数据失败: {}", e.getMessage());
            // 返回空数据
            for (int i = 0; i < days; i++) {
                LocalDate date = startDate.plusDays(i);
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", date.toString());
                dayData.put("count", 0L);
                result.add(dayData);
            }
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
        String sql = "SELECT id, reporter_id, target_type, reason, description, status, create_time FROM report ORDER BY create_time DESC LIMIT 10";
        List<Map<String, Object>> reports = jdbcTemplate.queryForList(sql);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> report : reports) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", report.get("id"));
            item.put("reporterId", report.get("reporter_id"));
            item.put("targetType", report.get("target_type"));
            item.put("reason", report.get("reason"));
            item.put("description", report.get("description"));
            item.put("status", report.get("status"));
            item.put("createTime", report.get("create_time"));
            result.add(item);
        }

        return result;
    }
}

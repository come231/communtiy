package com.community.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.admin.entity.Report;
import com.community.admin.mapper.ReportMapper;
import com.community.admin.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    @Qualifier("primaryJdbcTemplate")
    private final JdbcTemplate primaryJdbcTemplate;

    @Qualifier("securityJdbcTemplate")
    private final JdbcTemplate securityJdbcTemplate;

    private final ReportMapper reportMapper;

    public DashboardVO getDashboardStats() {
        DashboardVO vo = new DashboardVO();

        try {
            // 总用户数（从安全库查询）
            vo.setTotalUserCount(securityJdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT user_id) FROM operation_log WHERE 1=1", Long.class));

            // 总帖子数（估算）
            vo.setTotalPostCount(0L);

            // 总评论数（估算）
            vo.setTotalCommentCount(0L);

            // 今日活跃用户
            LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
            vo.setTodayActiveUser(securityJdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT user_id) FROM operation_log WHERE create_time >= ?",
                    Long.class, todayStart));

            // 待处理举报数量
            vo.setPendingReportCount(reportMapper.selectCount(
                    new LambdaQueryWrapper<Report>().eq(Report::getStatus, 0)));
        } catch (Exception e) {
            vo.setTotalUserCount(0L);
            vo.setTodayActiveUser(0L);
            vo.setPendingReportCount(0L);
        }

        return vo;
    }

    public List<Map<String, Object>> getUserTrend(int days) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        try {
            List<Map<String, Object>> rows = securityJdbcTemplate.queryForList(
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
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<Report>()
                .orderByDesc(Report::getCreateTime)
                .last("LIMIT 10");

        List<Report> reports = reportMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Report report : reports) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", report.getId());
            item.put("reporterId", report.getReporterId());
            item.put("targetType", report.getTargetType());
            item.put("reason", report.getReason());
            item.put("status", report.getStatus());
            item.put("createTime", report.getCreateTime());
            result.add(item);
        }

        return result;
    }
}

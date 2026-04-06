package com.community.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.admin.entity.OperationLog;
import com.community.admin.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserManageService {

    @Qualifier("securityJdbcTemplate")
    private final JdbcTemplate securityJdbcTemplate;

    private final OperationLogMapper operationLogMapper;

    /**
     * 获取用户列表（分页查询）
     */
    public IPage<Map<String, Object>> getUserList(int page, int size, String keyword) {
        Page<Map<String, Object>> pageObj = new Page<>(page, size);

        StringBuilder sql = new StringBuilder(
                "SELECT u.user_id as userId, u.username as username, " +
                "COUNT(*) as activityCount, MAX(o.create_time) as lastActiveTime " +
                "FROM (" +
                "   SELECT DISTINCT user_id, username FROM operation_log");

        StringBuilder countSql = new StringBuilder(
                "SELECT COUNT(DISTINCT user_id) FROM operation_log");

        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" WHERE username LIKE ?");
            countSql.append(" WHERE username LIKE ?");
        }

        sql.append(") u LEFT JOIN operation_log o ON u.user_id = o.user_id GROUP BY u.user_id, u.username");

        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isEmpty()) {
            params.add("%" + keyword + "%");
        }

        long total;
        try {
            total = securityJdbcTemplate.queryForObject(countSql.toString(), Long.class, params.toArray());
        } catch (Exception e) {
            total = 0;
        }

        sql.append(" ORDER BY lastActiveTime DESC LIMIT ?, ?");
        params.add((page - 1) * size);
        params.add(size);

        List<Map<String, Object>> records;
        try {
            records = securityJdbcTemplate.queryForList(sql.toString(), params.toArray());
        } catch (Exception e) {
            records = new ArrayList<>();
        }

        pageObj.setRecords(records);
        pageObj.setTotal(total);
        return pageObj;
    }

    /**
     * 获取用户详情
     */
    public Map<String, Object> getUserDetail(Long userId) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("userId", userId);

        try {
            // 用户基本信息
            Map<String, Object> userInfo = securityJdbcTemplate.queryForMap(
                    "SELECT user_id, username, COUNT(*) as totalOperations, " +
                    "MAX(create_time) as lastActiveTime, MIN(create_time) as firstActiveTime " +
                    "FROM operation_log WHERE user_id = ? GROUP BY user_id, username", userId);
            detail.putAll(userInfo);

            // 最近操作记录
            List<Map<String, Object>> recentOps = securityJdbcTemplate.queryForList(
                    "SELECT module, operation, url, ip, create_time FROM operation_log " +
                    "WHERE user_id = ? ORDER BY create_time DESC LIMIT 10", userId);
            detail.put("recentOperations", recentOps);

            // 操作统计按模块分组
            List<Map<String, Object>> statsByModule = securityJdbcTemplate.queryForList(
                    "SELECT module, COUNT(*) as count FROM operation_log WHERE user_id = ? " +
                    "GROUP BY module ORDER BY count DESC", userId);
            detail.put("statsByModule", statsByModule);
        } catch (Exception e) {
            detail.put("totalOperations", 0);
            detail.put("recentOperations", new ArrayList<>());
            detail.put("statsByModule", new ArrayList<>());
        }

        return detail;
    }

    /**
     * 封禁用户（写入IP黑名单）
     */
    public void banUser(Long userId, String reason, Integer days) {
        // 查找用户的IP地址
        String ip = null;
        try {
            Map<String, Object> userRecord = securityJdbcTemplate.queryForMap(
                    "SELECT DISTINCT ip FROM operation_log WHERE user_id = ? ORDER BY create_time DESC LIMIT 1", userId);
            ip = (String) userRecord.get("ip");
        } catch (Exception e) {
            // 忽略
        }

        if (ip != null) {
            java.time.LocalDateTime expireTime = days > 0
                    ? java.time.LocalDateTime.now().plusDays(days)
                    : java.time.LocalDateTime.now().plusYears(100);

            securityJdbcTemplate.update(
                    "INSERT INTO ip_blacklist (ip, reason, status, create_time, expire_time) VALUES (?, ?, 1, NOW(), ?)",
                    ip, reason != null ? reason : "违规操作封禁", expireTime);
        }
    }

    /**
     * 解封用户
     */
    public void unbanUser(Long userId) {
        try {
            String ip = null;
            Map<String, Object> userRecord = securityJdbcTemplate.queryForMap(
                    "SELECT DISTINCT ip FROM operation_log WHERE user_id = ? ORDER BY create_time DESC LIMIT 1", userId);
            ip = (String) userRecord.get("ip");

            if (ip != null) {
                securityJdbcTemplate.update(
                        "UPDATE ip_blacklist SET status = 0 WHERE ip = ?", ip);
            }
        } catch (Exception e) {
            // 忽略
        }
    }
}

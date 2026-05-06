package com.community.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.admin.feign.UserFeignClient;

import com.community.admin.mapper.security.OperationLogMapper;
import com.community.common.exception.BusinessException;
import com.community.common.result.PageResult;
import com.community.common.result.Result;
import com.community.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserManageService {

    private final JdbcTemplate jdbcTemplate; // 现在只有一个jdbcTemplate，连接到security库
    private final OperationLogMapper operationLogMapper;
    private final UserFeignClient userFeignClient;

    /**
     * 获取用户列表（分页查询，支持状态、角色筛选）
     */
    public IPage<Map<String, Object>> getUserList(int page, int size, String keyword, Integer status, Integer role) {
        try {
            // 调用用户服务的Feign接口搜索用户
            Result<Map<String, Object>> result = userFeignClient.searchUsers(keyword, page, size, status, role);
            
            if (!result.isSuccess()) {
                log.error("获取用户列表失败: {}", result.getMessage());
                throw new BusinessException(ResultCode.FAILED, "获取用户列表失败");
            }
            
            Map<String, Object> searchResult = result.getData();
            PageResult<Map<String, Object>> pageResult = new PageResult<>();
            // ✅ 修复这里：安全转换数字类型，解决 Integer 转 Long 异常
            Object totalObj = searchResult.getOrDefault("total", 0L);
            long total = 0;
            if (totalObj instanceof Number) {
                total = ((Number) totalObj).longValue();
            }
            pageResult.setTotal(total);
            pageResult.setRecords((List<Map<String, Object>>) searchResult.getOrDefault("records", new ArrayList<>()));
            
            // 转换为IPage格式
            Page<Map<String, Object>> pageObj = new Page<>(page, size);
            pageObj.setTotal(pageResult.getTotal());
            pageObj.setRecords(pageResult.getRecords());
            
            return pageObj;
        } catch (Exception e) {
            log.error("调用用户服务获取用户列表异常: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.FAILED, "服务调用异常: " + e.getMessage());
        }
    }

    /**
     * 更新用户状态
     */
    public void updateUserStatus(Long userId, Integer status) {
        try {
            Result<Void> result = userFeignClient.updateUserStatus(userId, status);
            if (!result.isSuccess()) {
                throw new BusinessException(ResultCode.FAILED, "更新用户状态失败: " + result.getMessage());
            }
            log.info("更新用户状态成功: userId={}, status={}", userId, status);
        } catch (Exception e) {
            log.error("更新用户状态异常: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.FAILED, "更新用户状态失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户角色
     */
    public void updateUserRole(Long userId, Integer role) {
        try {
            Result<Void> result = userFeignClient.updateUserRole(userId, role);
            if (!result.isSuccess()) {
                throw new BusinessException(ResultCode.FAILED, "更新用户角色失败: " + result.getMessage());
            }
            log.info("更新用户角色成功: userId={}, role={}", userId, role);
        } catch (Exception e) {
            log.error("更新用户角色异常: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.FAILED, "更新用户角色失败: " + e.getMessage());
        }
    }

    /**
     * 禁言用户
     */
    public void muteUser(Long userId, Integer minutes, String reason) {
        try {
            Result<Void> result = userFeignClient.muteUser(userId, minutes, reason);
            if (!result.isSuccess()) {
                throw new BusinessException(ResultCode.FAILED, "禁言用户失败: " + result.getMessage());
            }
            log.info("禁言用户成功: userId={}, minutes={}, reason={}", userId, minutes, reason);
        } catch (Exception e) {
            log.error("禁言用户异常: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.FAILED, "禁言用户失败: " + e.getMessage());
        }
    }

    /**
     * 解除禁言
     */
    public void unmuteUser(Long userId) {
        try {
            Result<Void> result = userFeignClient.unmuteUser(userId);
            if (!result.isSuccess()) {
                throw new BusinessException(ResultCode.FAILED, "解除禁言失败: " + result.getMessage());
            }
            log.info("解除禁言成功: userId={}", userId);
        } catch (Exception e) {
            log.error("解除禁言异常: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.FAILED, "解除禁言失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新用户信息
     */
    public void updateUser(Long userId, Map<String, Object> userInfo) {
        try {
            Result<Void> result = userFeignClient.updateUser(userId, userInfo);
            if (!result.isSuccess()) {
                throw new BusinessException(ResultCode.FAILED, "更新用户信息失败: " + result.getMessage());
            }
            log.info("更新用户信息成功: userId={}, fields={}", userId, userInfo.keySet());
        } catch (Exception e) {
            log.error("更新用户信息异常: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.FAILED, "更新用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户详情
     */
    public Map<String, Object> getUserDetail(Long userId) {
        try {
            // 调用用户服务的Feign接口获取用户信息
            Result<Map<String, Object>> result = userFeignClient.getUserInfo(userId);
            
            if (!result.isSuccess()) {
                log.error("获取用户详情失败: {}", result.getMessage());
                throw new BusinessException(ResultCode.FAILED, "获取用户详情失败");
            }
            
            Map<String, Object> userInfo = result.getData();
            
            // 从安全库获取用户操作日志信息
            Map<String, Object> detail = new HashMap<>();
            detail.putAll(userInfo);
            
            try {
                // 用户操作统计
                Map<String, Object> operationInfo = jdbcTemplate.queryForMap(
                        "SELECT user_id, username, COUNT(*) as totalOperations, " +
                        "MAX(create_time) as lastActiveTime, MIN(create_time) as firstActiveTime " +
                        "FROM operation_log WHERE user_id = ? GROUP BY user_id, username", userId);
                detail.putAll(operationInfo);

                // 最近操作记录
                List<Map<String, Object>> recentOps = jdbcTemplate.queryForList(
                        "SELECT module, operation, url, ip, create_time FROM operation_log " +
                        "WHERE user_id = ? ORDER BY create_time DESC LIMIT 10", userId);
                detail.put("recentOperations", recentOps);

                // 操作统计按模块分组
                List<Map<String, Object>> statsByModule = jdbcTemplate.queryForList(
                        "SELECT module, COUNT(*) as count FROM operation_log WHERE user_id = ? " +
                        "GROUP BY module ORDER BY count DESC", userId);
                detail.put("statsByModule", statsByModule);
            } catch (Exception e) {
                detail.put("totalOperations", 0);
                detail.put("recentOperations", new ArrayList<>());
                detail.put("statsByModule", new ArrayList<>());
            }
            
            return detail;
        } catch (Exception e) {
            log.error("调用用户服务获取用户详情异常: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.FAILED, "服务调用异常: " + e.getMessage());
        }
    }

    /**
     * 封禁用户（禁用账户 + 写入IP黑名单）
     */
    public void banUser(Long userId, String reason, Integer days) {
        // 通过Feign调用用户服务禁用账户
        try {
            userFeignClient.updateUserStatus(userId, 0);
        } catch (Exception e) {
            log.warn("调用用户服务禁用账户失败: {}", e.getMessage());
        }

        // 同时写入IP黑名单
        try {
            String ip = null;
            Map<String, Object> userRecord = jdbcTemplate.queryForMap(
                    "SELECT DISTINCT ip FROM operation_log WHERE user_id = ? ORDER BY create_time DESC LIMIT 1", userId);
            ip = (String) userRecord.get("ip");

            if (ip != null) {
                java.time.LocalDateTime expireTime = days != null && days > 0
                        ? java.time.LocalDateTime.now().plusDays(days)
                        : java.time.LocalDateTime.now().plusYears(100);

                jdbcTemplate.update(
                        "INSERT INTO ip_blacklist (ip, reason, status, create_time, expire_time) VALUES (?, ?, 1, NOW(), ?)",
                        ip, reason != null ? reason : "违规操作封禁", expireTime);
            }
        } catch (Exception e) {
            log.warn("写入IP黑名单失败: {}", e.getMessage());
        }
    }

    /**
     * 解封用户
     */
    public void unbanUser(Long userId) {
        // 通过Feign调用用户服务启用账户
        try {
            userFeignClient.updateUserStatus(userId, 1);
        } catch (Exception e) {
            log.warn("调用用户服务启用账户失败: {}", e.getMessage());
        }

        // 清除IP黑名单
        try {
            String ip = null;
            Map<String, Object> userRecord = jdbcTemplate.queryForMap(
                    "SELECT DISTINCT ip FROM operation_log WHERE user_id = ? ORDER BY create_time DESC LIMIT 1", userId);
            ip = (String) userRecord.get("ip");

            if (ip != null) {
                jdbcTemplate.update(
                        "UPDATE ip_blacklist SET status = 0 WHERE ip = ?", ip);
            }
        } catch (Exception e) {
            log.warn("清除IP黑名单失败: {}", e.getMessage());
        }
    }
}

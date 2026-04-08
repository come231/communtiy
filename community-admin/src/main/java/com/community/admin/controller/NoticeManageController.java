package com.community.admin.controller;

import com.community.common.result.PageResult;
import com.community.common.result.Result;
import com.community.admin.mapper.security.SensitiveWordMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知管理
 */
@RestController
@RequestMapping("/admin/notices")
@RequiredArgsConstructor
@Tag(name = "通知管理", description = "管理员通知管理接口")
public class NoticeManageController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    @Operation(summary = "通知列表", description = "获取所有通知列表")
    public Result<PageResult<Map<String, Object>>> getNotices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer type) {
        try {
            int offset = (page - 1) * size;
            StringBuilder sql = new StringBuilder("SELECT id, user_id, content, type, from_uid, target_id, is_read, create_time FROM notice WHERE 1=1");
            StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM notice WHERE 1=1");
            java.util.List<Object> params = new ArrayList<>();

            if (type != null) {
                sql.append(" AND type = ?");
                countSql.append(" AND type = ?");
                params.add(type);
            }

            sql.append(" ORDER BY create_time DESC LIMIT ? OFFSET ?");
            params.add(size);
            params.add(offset);

            List<Map<String, Object>> records = jdbcTemplate.queryForList(sql.toString(), params.toArray());
            Long total = jdbcTemplate.queryForObject(countSql.toString(), params.subList(0, params.size() - 2).toArray(), Long.class);

            PageResult<Map<String, Object>> pageResult = new PageResult<>(records, total, size, page);
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error("获取通知列表失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知", description = "删除指定通知")
    public Result<Void> deleteNotice(@PathVariable Long id) {
        try {
            jdbcTemplate.update("DELETE FROM notice WHERE id = ?", id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除通知失败: " + e.getMessage());
        }
    }
}

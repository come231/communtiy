package com.community.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostManageService {

    @Qualifier("contentJdbcTemplate")
    private final JdbcTemplate contentJdbcTemplate;

    /**
     * 获取帖子列表
     */
    public IPage<Map<String, Object>> getPostList(int page, int size, String keyword, Integer status) {
        StringBuilder sql = new StringBuilder("SELECT p.*, u.username, u.nickname, u.avatar, ");
        sql.append(" (SELECT COUNT(*) FROM post_report pr WHERE pr.post_id = p.post_id) as report_count ");
        sql.append(" FROM post p LEFT JOIN user u ON p.user_id = u.user_id WHERE 1=1 ");

        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM post p WHERE 1=1 ");

        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (p.title LIKE ? OR p.content LIKE ?) ");
            countSql.append(" AND (p.title LIKE ? OR p.content LIKE ?) ");
        }
        if (status != null) {
            sql.append(" AND p.status = ? ");
            countSql.append(" AND p.status = ? ");
        }

        // 排序
        sql.append(" ORDER BY p.create_time DESC ");

        // 分页
        int offset = (page - 1) * size;
        sql.append(" LIMIT ? OFFSET ? ");

        // 构建参数
        java.util.List<Object> params = new java.util.ArrayList<>();
        java.util.List<Object> countParams = new java.util.ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            String likeKeyword = "%" + keyword + "%";
            params.add(likeKeyword);
            params.add(likeKeyword);
            countParams.add(likeKeyword);
            countParams.add(likeKeyword);
        }
        if (status != null) {
            params.add(status);
            countParams.add(status);
        }
        params.add(size);
        params.add(offset);

        // 查询总数
        long total = 0;
        if (!countParams.isEmpty()) {
            total = contentJdbcTemplate.queryForObject(countSql.toString(), countParams.toArray(), Long.class);
        } else {
            total = contentJdbcTemplate.queryForObject(countSql.toString(), Long.class);
        }

        // 查询数据
        List<Map<String, Object>> records = contentJdbcTemplate.queryForList(sql.toString(), params.toArray());

        // 转换结果
        Page<Map<String, Object>> resultPage = new Page<>(page, size);
        resultPage.setTotal(total);
        resultPage.setRecords(records);

        return resultPage;
    }

    /**
     * 获取帖子详情
     */
    public Map<String, Object> getPostDetail(Long id) {
        String sql = "SELECT p.*, u.username, u.nickname, u.avatar " +
                     "FROM post p LEFT JOIN user u ON p.user_id = u.user_id " +
                     "WHERE p.post_id = ?";
        List<Map<String, Object>> results = contentJdbcTemplate.queryForList(sql, id);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 审核帖子
     */
    public void reviewPost(Long id, Integer status, String reason) {
        String sql = "UPDATE post SET status = ?, update_time = ? WHERE post_id = ?";
        contentJdbcTemplate.update(sql, status, LocalDateTime.now(), id);
        log.info("Post {} reviewed with status {}, reason: {}", id, status, reason);
    }

    /**
     * 设置置顶
     */
    public void setTop(Long id, boolean isTop) {
        String sql = "UPDATE post SET is_top = ?, update_time = ? WHERE post_id = ?";
        contentJdbcTemplate.update(sql, isTop ? 1 : 0, LocalDateTime.now(), id);
    }

    /**
     * 设置精华
     */
    public void setEssence(Long id, boolean isEssence) {
        String sql = "UPDATE post SET is_essence = ?, update_time = ? WHERE post_id = ?";
        contentJdbcTemplate.update(sql, isEssence ? 1 : 0, LocalDateTime.now(), id);
    }

    /**
     * 删除帖子
     */
    public void deletePost(Long id) {
        String sql = "UPDATE post SET status = -1, update_time = ? WHERE post_id = ?";
        contentJdbcTemplate.update(sql, LocalDateTime.now(), id);
        log.info("Post {} deleted", id);
    }
}

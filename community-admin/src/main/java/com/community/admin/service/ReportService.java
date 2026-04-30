package com.community.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.admin.dto.HandleReportDTO;
import com.community.admin.entity.Report;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final JdbcTemplate jdbcTemplate;

    public IPage<Report> getReportList(int page, int size, Integer status) {
        Page<Report> pageObj = new Page<>(page, size);

        StringBuilder sql = new StringBuilder("SELECT * FROM report WHERE 1=1 ");
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM report WHERE 1=1 ");

        if (status != null) {
            sql.append(" AND status = ? ");
            countSql.append(" AND status = ? ");
        }

        sql.append(" ORDER BY create_time DESC LIMIT ? OFFSET ?");

        int offset = (page - 1) * size;
        Object[] params;
        Object[] countParams;

        if (status != null) {
            params = new Object[]{status, size, offset};
            countParams = new Object[]{status};
        } else {
            params = new Object[]{size, offset};
            countParams = new Object[]{};
        }

        Long total = jdbcTemplate.queryForObject(countSql.toString(), countParams, Long.class);
        List<Report> records = jdbcTemplate.query(sql.toString(), params, new BeanPropertyRowMapper<>(Report.class));

        pageObj.setRecords(records);
        pageObj.setTotal(total != null ? total : 0);
        return pageObj;
    }

    public boolean handleReport(HandleReportDTO dto, Long handlerId) {
        String querySql = "SELECT * FROM report WHERE id = ?";
        List<Report> reports = jdbcTemplate.query(querySql, new BeanPropertyRowMapper<>(Report.class), dto.getReportId());

        if (reports.isEmpty()) {
            throw new RuntimeException("举报记录不存在");
        }

        Report report = reports.get(0);
        if (report.getStatus() != 0) {
            throw new RuntimeException("该举报已处理");
        }

        int newStatus = dto.getStatus() != null ? dto.getStatus() : 1;
        String updateSql = "UPDATE report SET status = ?, handler_id = ?, handle_result = ?, handle_time = NOW() WHERE id = ?";
        int updated = jdbcTemplate.update(updateSql, newStatus, handlerId, dto.getHandleResult(), dto.getReportId());

        return updated > 0;
    }

    public Map<Integer, Long> getReportCount() {
        Map<Integer, Long> result = new HashMap<>();
        result.put(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM report WHERE status = 0", Long.class));
        result.put(1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM report WHERE status = 1", Long.class));
        result.put(2, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM report WHERE status = 2", Long.class));
        result.put(-1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM report", Long.class));
        return result;
    }
}
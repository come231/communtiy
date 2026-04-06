package com.community.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.admin.dto.HandleReportDTO;
import com.community.admin.entity.Report;
import com.community.admin.mapper.ReportMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService extends ServiceImpl<ReportMapper, Report> {

    private final ReportMapper reportMapper;

    public IPage<Report> getReportList(int page, int size, Integer status) {
        Page<Report> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(Report::getStatus, status);
        }

        wrapper.orderByDesc(Report::getCreateTime);
        return reportMapper.selectPage(pageObj, wrapper);
    }

    public boolean handleReport(HandleReportDTO dto, Long handlerId) {
        Report report = reportMapper.selectById(dto.getReportId());
        if (report == null) {
            throw new RuntimeException("举报记录不存在");
        }

        if (report.getStatus() != 0) {
            throw new RuntimeException("该举报已处理");
        }

        report.setStatus(1); // 已处理
        report.setHandlerId(handlerId);
        report.setHandleResult(dto.getHandleResult());
        report.setHandleTime(java.time.LocalDateTime.now());

        return reportMapper.updateById(report) > 0;
    }

    public Map<Integer, Long> getReportCount() {
        Map<Integer, Long> result = new HashMap<>();

        // 待处理数量
        result.put(0, reportMapper.selectCount(
                new LambdaQueryWrapper<Report>().eq(Report::getStatus, 0)));

        // 已处理数量
        result.put(1, reportMapper.selectCount(
                new LambdaQueryWrapper<Report>().eq(Report::getStatus, 1)));

        // 已驳回数量
        result.put(2, reportMapper.selectCount(
                new LambdaQueryWrapper<Report>().eq(Report::getStatus, 2)));

        // 总数
        result.put(-1, reportMapper.selectCount(new LambdaQueryWrapper<>()));

        return result;
    }
}

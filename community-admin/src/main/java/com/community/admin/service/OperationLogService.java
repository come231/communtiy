package com.community.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.admin.entity.OperationLog;

import com.community.admin.mapper.security.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;

    public IPage<OperationLog> getLogs(int page, int size, String module,
                                      LocalDateTime startTime, LocalDateTime endTime) {
        Page<OperationLog> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();

        if (module != null && !module.isEmpty()) {
            wrapper.eq(OperationLog::getModule, module);
        }

        if (startTime != null) {
            wrapper.ge(OperationLog::getCreateTime, startTime);
        }

        if (endTime != null) {
            wrapper.le(OperationLog::getCreateTime, endTime);
        }

        wrapper.orderByDesc(OperationLog::getCreateTime);

        return operationLogMapper.selectPage(pageObj, wrapper);
    }

    /**
     * 获取所有模块列表（用于筛选下拉框）
     */
    public List<String> getModuleList() {
        return operationLogMapper.selectList(
                        new LambdaQueryWrapper<OperationLog>()
                                .select(OperationLog::getModule)
                                .groupBy(OperationLog::getModule))
                .stream()
                .map(OperationLog::getModule)
                .filter(m -> m != null && !m.isEmpty())
                .toList();
    }
}

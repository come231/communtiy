package com.community.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.admin.feign.AiFeignClient;
import com.community.common.dto.AiCheckRecordVO;
import com.community.common.exception.BusinessException;
import com.community.common.result.PageResult;
import com.community.common.result.Result;
import com.community.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiCheckRecordAdminService {

    private final AiFeignClient aiFeignClient;

    public IPage<AiCheckRecordVO> list(int page, int size, Long userId, Long postId, Integer checkResult) {
        try {
            Result<PageResult<AiCheckRecordVO>> result = aiFeignClient.listCheckRecords(page, size, userId, postId, checkResult);
            if (!result.isSuccess()) {
                throw new BusinessException(ResultCode.FAILED, result.getMessage() != null ? result.getMessage() : "查询失败");
            }
            PageResult<AiCheckRecordVO> pr = result.getData();
            Page<AiCheckRecordVO> p = new Page<>(page, size);
            if (pr != null) {
                p.setRecords(pr.getRecords());
                p.setTotal(pr.getTotal());
            }
            return p;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用 AI 服务查询审核记录异常: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.FAILED, "服务调用异常: " + e.getMessage());
        }
    }
}

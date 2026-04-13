package com.community.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.admin.feign.MessageFeignClient;
import com.community.admin.vo.ChatMsgVO;
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
public class NoticeManageService {

    private final MessageFeignClient messageFeignClient;

    public IPage<ChatMsgVO> getNoticeList(Integer type, int page, int size) {
        try {
            Result<PageResult<ChatMsgVO>> res = messageFeignClient.getAdminNotices(type, page, size);
            if (!res.isSuccess()) {
                throw new BusinessException(res.getMessage());
            }
            PageResult<ChatMsgVO> data = res.getData();
            Page<ChatMsgVO> pageResult = new Page<>(page, size);
            pageResult.setRecords(data.getRecords());
            pageResult.setTotal(data.getTotal());
            return pageResult;
        } catch (Exception e) {
            log.error("获取通知失败: {}", e.getMessage());
            throw new BusinessException("获取通知失败");
        }
    }

    public void deleteNotice(Long id) {
        try {
            messageFeignClient.adminDeleteNotice(id);
        } catch (Exception e) {
            throw new BusinessException("删除失败");
        }
    }
}
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
@RequiredArgsConstructor
@Slf4j
public class MessageManageService {

    private final MessageFeignClient messageFeignClient;

    /**
     * 管理员获取所有聊天消息（分页）
     */
    public IPage<ChatMsgVO> getAdminMessageList(int page, int size) {
        try {
            Result<PageResult<ChatMsgVO>> result = messageFeignClient.getAdminChatList(page, size);
            if (!result.isSuccess()) {
                log.error("获取消息列表失败: {}", result.getMessage());
                throw new BusinessException(ResultCode.FAILED, "获取消息列表失败");
            }

            PageResult<ChatMsgVO> pageResult = result.getData();
            Page<ChatMsgVO> pageObj = new Page<>(page, size);
            pageObj.setTotal(pageResult.getTotal());
            pageObj.setRecords(pageResult.getRecords());
            return pageObj;
        } catch (Exception e) {
            log.error("获取消息列表异常: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.FAILED, "服务调用异常");
        }
    }
    /**
     * 管理员根据用户ID查询聊天记录
     */
    public IPage<ChatMsgVO> getMessageListByUserId(Long userId, int page, int size) {
        try {
            Result<PageResult<ChatMsgVO>> result = messageFeignClient.getAdminChatListByUserId(userId, page, size);
            if (!result.isSuccess()) {
                log.error("获取用户消息失败: {}", result.getMessage());
                throw new BusinessException(ResultCode.FAILED, "获取用户消息失败");
            }

            PageResult<ChatMsgVO> pageResult = result.getData();
            Page<ChatMsgVO> pageObj = new Page<>(page, size);
            pageObj.setTotal(pageResult.getTotal());
            pageObj.setRecords(pageResult.getRecords());
            return pageObj;
        } catch (Exception e) {
            log.error("获取用户消息异常: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.FAILED, "服务调用异常");
        }
    }
    /**
     * 管理员删除消息
     */
    public void deleteMessage(Long id) {
        try {
            messageFeignClient.adminDeleteMessage(id);
        } catch (Exception e) {
            log.error("删除消息失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.FAILED, "删除消息失败");
        }
    }
}
package com.community.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.admin.feign.PostFeignClient;
import com.community.common.exception.BusinessException;
import com.community.common.result.PageResult;
import com.community.common.result.Result;
import com.community.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostManageService {

    private final PostFeignClient postFeignClient;

    /**
     * 获取帖子列表
     */
    public IPage<Map<String, Object>> getPostList(int page, int size, String keyword, Integer status) {
        try {
            // 调用社区服务的Feign接口获取帖子列表
            Result<PageResult<Map<String, Object>>> result = postFeignClient.listPosts(
                    keyword, null, null, "latest", page, size);
            
            if (!result.isSuccess()) {
                log.error("获取帖子列表失败: {}", result.getMessage());
                throw new BusinessException(ResultCode.FAILED, "获取帖子列表失败");
            }
            
            PageResult<Map<String, Object>> pageResult = result.getData();
            
            // 转换为IPage格式
            Page<Map<String, Object>> resultPage = new Page<>(page, size);
            resultPage.setTotal(pageResult.getTotal());
            resultPage.setRecords(pageResult.getRecords());
            
            return resultPage;
        } catch (Exception e) {
            log.error("调用社区服务获取帖子列表异常: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.FAILED, "服务调用异常: " + e.getMessage());
        }
    }

    /**
     * 获取帖子详情
     */
    public Map<String, Object> getPostDetail(Long id) {
        try {
            Result<Map<String, Object>> result = postFeignClient.getPostDetail(id);
            
            if (!result.isSuccess()) {
                log.error("获取帖子详情失败: {}", result.getMessage());
                throw new BusinessException(ResultCode.FAILED, "获取帖子详情失败");
            }
            
            return result.getData();
        } catch (Exception e) {
            log.error("调用社区服务获取帖子详情异常: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.FAILED, "服务调用异常: " + e.getMessage());
        }
    }

    /**
     * 审核帖子
     */
    public void reviewPost(Long id, Integer status, String reason) {
        try {
            Result<Void> result = postFeignClient.reviewPost(id, status, reason);
            
            if (!result.isSuccess()) {
                log.error("审核帖子失败: {}", result.getMessage());
                throw new BusinessException(ResultCode.FAILED, "审核帖子失败");
            }
            
            log.info("Post {} reviewed with status {}, reason: {}", id, status, reason);
        } catch (Exception e) {
            log.error("调用社区服务审核帖子异常: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.FAILED, "服务调用异常: " + e.getMessage());
        }
    }

    /**
     * 设置置顶
     */
    public void setTop(Long id, boolean isTop) {
        try {
            Result<Void> result = postFeignClient.setTop(id, isTop);
            
            if (!result.isSuccess()) {
                log.error("设置置顶失败: {}", result.getMessage());
                throw new BusinessException(ResultCode.FAILED, "设置置顶失败");
            }
        } catch (Exception e) {
            log.error("调用社区服务设置置顶异常: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.FAILED, "服务调用异常: " + e.getMessage());
        }
    }

    /**
     * 设置精华
     */
    public void setEssence(Long id, boolean isEssence) {
        try {
            Result<Void> result = postFeignClient.setEssence(id, isEssence);
            
            if (!result.isSuccess()) {
                log.error("设置精华失败: {}", result.getMessage());
                throw new BusinessException(ResultCode.FAILED, "设置精华失败");
            }
        } catch (Exception e) {
            log.error("调用社区服务设置精华异常: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.FAILED, "服务调用异常: " + e.getMessage());
        }
    }

    /**
     * 删除帖子
     */
    public void deletePost(Long id) {
        try {
            Result<Void> result = postFeignClient.deletePost(id);
            
            if (!result.isSuccess()) {
                log.error("删除帖子失败: {}", result.getMessage());
                throw new BusinessException(ResultCode.FAILED, "删除帖子失败");
            }
            
            log.info("Post {} deleted", id);
        } catch (Exception e) {
            log.error("调用社区服务删除帖子异常: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.FAILED, "服务调用异常: " + e.getMessage());
        }
    }
}
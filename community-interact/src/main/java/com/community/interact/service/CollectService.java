package com.community.interact.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.annotation.OperationLog;
import com.community.common.constant.CommonConstant;
import com.community.common.constant.MQConstant;
import com.community.common.constant.RedisKeyConstant;
import com.community.common.exception.BusinessException;
import com.community.common.result.PageResult;
import com.community.common.result.ResultCode;
import com.community.common.util.ContextHolder;
import com.community.common.util.RedisUtil;
import com.community.common.result.Result;
import com.community.interact.dto.CollectVO;
import com.community.interact.entity.Collect;
import com.community.interact.entity.CollectFolder;
import com.community.interact.feign.CommunityPostFeignClient;
import com.community.interact.mapper.CollectFolderMapper;
import com.community.interact.mapper.CollectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectService {

    private final CollectMapper collectMapper;
    private final CollectFolderMapper collectFolderMapper;
    private final RedisUtil redisUtil;
    private final RedissonClient redissonClient;
    private final RabbitTemplate rabbitTemplate;
    private final CommunityPostFeignClient communityPostFeignClient;

    public boolean isCollected(Long userId, Long postId) {
        return collectMapper.selectCount(
                new LambdaQueryWrapper<Collect>()
                        .eq(Collect::getUserId, userId)
                        .eq(Collect::getPostId, postId)) > 0;
    }

    @OperationLog(module = "互动", operation = "收藏帖子")
    @SuppressWarnings("unchecked")
    public void collect(Long userId, Long postId, Long folderId) {
        String lockKey = "lock:collect:" + userId + ":" + postId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                throw new BusinessException("操作过于频繁");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("操作被中断");
        }
        try {
            long count = collectMapper.selectCount(
                    new LambdaQueryWrapper<Collect>()
                            .eq(Collect::getUserId, userId)
                            .eq(Collect::getPostId, postId));
            if (count > 0) throw new BusinessException(ResultCode.ALREADY_COLLECTED);

            Collect collect = new Collect();
            collect.setUserId(userId);
            collect.setPostId(postId);
            collect.setFolderId(folderId);
            collect.setCreateTime(LocalDateTime.now());
            collectMapper.insert(collect);

            redisUtil.increment(RedisKeyConstant.POST_COLLECT_COUNT + postId);

            Map<String, Object> msg = new HashMap<>();
            msg.put("userId", userId);
            msg.put("postId", postId);
            msg.put("action", "collect");
            msg.put("createTime", LocalDateTime.now());
            rabbitTemplate.convertAndSend(MQConstant.COMMUNITY_EXCHANGE, MQConstant.COLLECT_ROUTING_KEY, msg);

            log.info("用户{}收藏了帖子{}", userId, postId);
        } finally { lock.unlock(); }
    }

    @OperationLog(module = "互动", operation = "取消收藏")
    @SuppressWarnings("unchecked")
    public void uncollect(Long userId, Long postId) {
        collectMapper.delete(new LambdaQueryWrapper<Collect>()
                .eq(Collect::getUserId, userId).eq(Collect::getPostId, postId));
        redisUtil.decrement(RedisKeyConstant.POST_COLLECT_COUNT + postId);

        Map<String, Object> msg = new HashMap<>();
        msg.put("userId", userId); msg.put("postId", postId);
        msg.put("action", "uncollect"); msg.put("createTime", LocalDateTime.now());
        rabbitTemplate.convertAndSend(MQConstant.COMMUNITY_EXCHANGE, MQConstant.COLLECT_ROUTING_KEY, msg);
    }

    public PageResult<CollectVO> listCollects(Long userId, Long folderId,
                                            int pageNum, int pageSize) {
        Page<Collect> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getUserId, userId);
        if (folderId != null) wrapper.eq(Collect::getFolderId, folderId);
        wrapper.orderByDesc(Collect::getCreateTime);
        Page<Collect> result = collectMapper.selectPage(page, wrapper);

        List<CollectVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, result.getTotal(), result.getSize(), result.getCurrent());
    }

    private CollectVO convertToVO(Collect collect) {
        CollectVO vo = new CollectVO();
        vo.setId(collect.getId());
        vo.setUserId(collect.getUserId());
        vo.setPostId(collect.getPostId());
        vo.setFolderId(collect.getFolderId());
        vo.setCreateTime(collect.getCreateTime());
        vo.setIsHidden(0);
        
        // 从Redis获取帖子信息
        String postInfo = redisUtil.get(RedisKeyConstant.POST_INFO_PREFIX + collect.getPostId());
        if (postInfo != null) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> postData = mapper.readValue(postInfo, Map.class);
                vo.setPostTitle((String) postData.get("title"));
                vo.setPostSummary((String) postData.get("summary"));
            } catch (Exception e) {
                log.warn("解析帖子信息失败: {}", e.getMessage());
            }
        }
        
        if (vo.getPostTitle() == null) {
            try {
                Result<java.util.Map<String, Object>> res = communityPostFeignClient.getPostDetail(collect.getPostId());
                if (res != null && res.getData() != null) {
                    Object t = res.getData().get("title");
                    if (t != null) {
                        vo.setPostTitle(String.valueOf(t));
                    }
                    Object s = res.getData().get("summary");
                    if (s != null && vo.getPostSummary() == null) {
                        vo.setPostSummary(String.valueOf(s));
                    }
                }
            } catch (Exception e) {
                log.warn("拉取帖子标题失败 postId={}: {}", collect.getPostId(), e.getMessage());
            }
        }
        if (vo.getPostTitle() == null || vo.getPostTitle().isBlank()) {
            vo.setPostTitle("未命名帖子");
        }

        return vo;
    }

    public List<CollectFolder> listFolders(Long userId) {
        return collectFolderMapper.selectList(new LambdaQueryWrapper<CollectFolder>()
                .eq(CollectFolder::getUserId, userId)
                .orderByDesc(CollectFolder::getCreateTime));
    }

    public CollectFolder createFolder(Long userId, String name) {
        CollectFolder folder = new CollectFolder();
        folder.setUserId(userId);
        folder.setName(name);
        folder.setCreateTime(LocalDateTime.now());
        collectFolderMapper.insert(folder);
        return folder;
    }

    public void deleteFolder(Long userId, Long folderId) {
        CollectFolder folder = collectFolderMapper.selectById(folderId);
        if (folder != null && folder.getUserId().equals(userId)) {
            // 删除该文件夹下的所有收藏
            collectMapper.delete(new LambdaQueryWrapper<Collect>()
                    .eq(Collect::getFolderId, folderId));
            // 删除文件夹
            collectFolderMapper.deleteById(folderId);
        }
    }
}

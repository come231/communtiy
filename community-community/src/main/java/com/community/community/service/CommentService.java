package com.community.community.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.annotation.OperationLog;
import com.community.common.constant.CommonConstant;
import com.community.common.constant.RedisKeyConstant;
import com.community.common.constant.MQConstant;
import com.community.common.exception.BusinessException;
import com.community.common.result.PageResult;
import com.community.common.result.ResultCode;
import com.community.common.util.ContextHolder;
import com.community.common.util.RedisUtil;
import com.community.common.util.SensitiveWordFilter;
import com.community.community.dto.CommentDTO;
import com.community.community.dto.CommentVO;
import com.community.community.entity.Comment;
import com.community.community.entity.Post;
import com.community.community.mapper.CommentMapper;
import com.community.community.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final SensitiveWordFilter sensitiveWordFilter;
    private final RabbitTemplate rabbitTemplate;
    private final RedisUtil redisUtil;

    public PageResult<CommentVO> listComments(Long postId, Integer pageNum, Integer pageSize) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getPostId, postId)
              .eq(Comment::getStatus, CommonConstant.COMMENT_STATUS_NORMAL)
              .eq(Comment::getParentId, 0L)
              .orderByAsc(Comment::getCreateTime);
        Page<Comment> result = commentMapper.selectPage(page, wrapper);

        List<CommentVO> voList = result.getRecords().stream()
                .map(c -> convertToVO(c)).collect(Collectors.toList());
        return new PageResult<>(voList, result.getTotal(), result.getSize(), result.getCurrent());
    }

    @Transactional
    @OperationLog(module = "评论", operation = "发表评论")
    public Long createComment(Long userId, CommentDTO dto) {
        Post post = postMapper.selectById(dto.getPostId());
        if (post == null || post.getStatus() != CommonConstant.POST_STATUS_NORMAL) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }

        if (sensitiveWordFilter.containsSensitiveWord(dto.getContent())) {
            throw new BusinessException(ResultCode.SENSITIVE_WORD_FOUND);
        }

        // 计算楼层号
        Long floor = commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>().eq(Comment::getPostId, dto.getPostId())) + 1;

        Comment comment = new Comment();
        comment.setPostId(dto.getPostId());
        comment.setUserId(userId);
        comment.setContent(dto.getContent());
        comment.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        comment.setReplyUid(dto.getReplyUid() != null ? dto.getReplyUid() : 0L);
        comment.setLikeCount(0);
        comment.setStatus(CommonConstant.COMMENT_STATUS_NORMAL);
        comment.setFloor(floor.intValue());
        commentMapper.insert(comment);

        // 更新帖子评论数
        post.setCommentCount(post.getCommentCount() + 1);
        postMapper.updateById(post);

        // 发送评论通知MQ
        if (!userId.equals(post.getUserId())) {
            Map<String, Object> msg = new HashMap<>();
            msg.put("event", "comment");
            msg.put("targetUserId", post.getUserId());
            msg.put("operatorId", userId);
            msg.put("relatedId", dto.getPostId());
            msg.put("relatedType", 1); // 1=帖子
            msg.put("content", dto.getContent().length() > 50 ? dto.getContent().substring(0, 50) : dto.getContent());
            msg.put("createTime", LocalDateTime.now());
            rabbitTemplate.convertAndSend(MQConstant.COMMUNITY_EXCHANGE, MQConstant.NOTICE_ROUTING_KEY, msg);
        }

        log.info("用户{}在帖子{}发表评论: {}", userId, dto.getPostId(), comment.getCommentId());
        return comment.getCommentId();
    }

    @OperationLog(module = "评论", operation = "删除评论")
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
        if (!comment.getUserId().equals(userId)) throw new BusinessException("无权限删除此评论");

        comment.setStatus(CommonConstant.COMMENT_STATUS_DELETED);
        commentMapper.updateById(comment);

        // 更新帖子评论数
        Post post = postMapper.selectById(comment.getPostId());
        if (post != null && post.getCommentCount() > 0) {
            post.setCommentCount(post.getCommentCount() - 1);
            postMapper.updateById(post);
        }
    }

    public List<CommentVO> getChildComments(Long parentCommentId) {
        List<Comment> children = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getParentId, parentCommentId)
                        .eq(Comment::getStatus, CommonConstant.COMMENT_STATUS_NORMAL)
                        .orderByAsc(Comment::getCreateTime));
        return children.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    private CommentVO convertToVO(Comment comment) {
        CommentVO vo = new CommentVO();
        vo.setCommentId(comment.getCommentId());
        vo.setPostId(comment.getPostId());
        vo.setUserId(comment.getUserId());
        vo.setContent(comment.getContent());
        vo.setParentId(comment.getParentId());
        vo.setReplyUid(comment.getReplyUid());
        vo.setLikeCount(comment.getLikeCount());
        vo.setFloor(comment.getFloor());
        vo.setCreateTime(comment.getCreateTime());

        // 获取用户信息
        Long currentUserId = ContextHolder.getUserId();
        Map<String, String> authorInfo = getUserInfoFromCache(comment.getUserId());
        vo.setAuthorName(authorInfo.getOrDefault("nickname", "用户" + comment.getUserId()));
        vo.setAuthorAvatar(authorInfo.getOrDefault("avatar", ""));

        // 获取被回复用户信息
        if (comment.getReplyUid() != null && comment.getReplyUid() > 0) {
            Map<String, String> replyUserInfo = getUserInfoFromCache(comment.getReplyUid());
            vo.setReplyUserName(replyUserInfo.getOrDefault("nickname", "用户" + comment.getReplyUid()));
        }

        // 检查当前用户是否点赞
        if (currentUserId != null) {
            boolean isLiked = redisUtil.sIsMember(
                    RedisKeyConstant.USER_LIKE_COMMENT_PREFIX + currentUserId,
                    String.valueOf(comment.getCommentId())
            );
            vo.setIsLiked(isLiked);
        } else {
            vo.setIsLiked(false);
        }

        // 获取子回复
        if (comment.getParentId() == 0 && comment.getFloor() > 0) {
            List<CommentVO> replies = getChildComments(comment.getCommentId());
            vo.setReplies(replies);
        }
        return vo;
    }

    private Map<String, String> getUserInfoFromCache(Long userId) {
        Map<String, String> userInfo = new HashMap<>();
        if (userId == null) return userInfo;
        
        try {
            String key = RedisKeyConstant.USER_INFO_PREFIX + userId;
            String userInfoJson = redisUtil.get(key);
            if (userInfoJson != null && userInfoJson.startsWith("{")) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, Object> info = mapper.readValue(userInfoJson, Map.class);
                if (info.get("nickname") != null) {
                    userInfo.put("nickname", String.valueOf(info.get("nickname")));
                }
                if (info.get("avatar") != null) {
                    userInfo.put("avatar", String.valueOf(info.get("avatar")));
                }
            }
        } catch (Exception e) {
            log.debug("获取用户信息失败: {}", e.getMessage());
        }
        return userInfo;
    }
}

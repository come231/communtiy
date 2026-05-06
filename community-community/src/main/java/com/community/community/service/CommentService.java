package com.community.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.annotation.OperationLog;
import com.community.common.constant.CommonConstant;
import com.community.common.constant.RedisKeyConstant;
import com.community.common.constant.MQConstant;
import com.community.common.dto.FileInfoDTO;
import com.community.common.exception.BusinessException;
import com.community.common.feign.FileFeignClient;
import com.community.common.result.PageResult;
import com.community.common.result.Result;
import com.community.common.result.ResultCode;
import com.community.common.util.ContextHolder;
import com.community.common.util.RedisUtil;
import com.community.common.util.SensitiveWordFilter;
import com.community.community.dto.CommentDTO;
import com.community.community.dto.CommentVO;
import com.community.community.entity.Comment;
import com.community.community.entity.CommentMedia;
import com.community.community.entity.Post;
import com.community.community.mapper.CommentMapper;
import com.community.community.mapper.CommentMediaMapper;
import com.community.community.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论：发表/展示；若以后支持编辑，仅应更新本表/comment_media 引用，不主动删改历史上传的 OSS 对象
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private static final String FILE_DL = "/api/file/download/";
    private static final long COMMENT_MEDIA_MAX = 20L * 1024 * 1024;

    private final CommentMapper commentMapper;
    private final CommentMediaMapper commentMediaMapper;
    private final PostMapper postMapper;
    private final FileFeignClient fileFeignClient;
    private final SensitiveWordFilter sensitiveWordFilter;
    private final SensitiveFilterRecordService sensitiveFilterRecordService;
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

        String text = dto.getContent() != null ? dto.getContent().trim() : "";
        List<Long> imgIds = new ArrayList<>();
        if (dto.getImageFileIds() != null) {
            for (String s : dto.getImageFileIds()) {
                if (s == null || s.isBlank()) {
                    continue;
                }
                String t = s.trim();
                if (!t.matches("[0-9]+")) {
                    throw new BusinessException("非法图片文件ID");
                }
                try {
                    imgIds.add(Long.parseLong(t));
                } catch (NumberFormatException e) {
                    throw new BusinessException("非法图片文件ID");
                }
            }
        }
        if (text.isEmpty() && imgIds.isEmpty()) {
            throw new BusinessException("评论内容或图片至少填写一项");
        }
        String originalText = text;
        List<String> sensitiveHits = Collections.emptyList();
        // 评论采用替换模式：命中敏感词时替换为 *** 后再入库
        if (!text.isEmpty() && sensitiveWordFilter.containsSensitiveWord(text)) {
            sensitiveHits = sensitiveWordFilter.findSensitiveWords(text);
            String masked = sensitiveWordFilter.filter(text, "***");
            log.warn("评论命中敏感词，已替换后保存，postId={}, userId={}, hits={}", dto.getPostId(), userId, sensitiveHits);
            text = masked;
        }

        long imgBytes = 0;
        List<FileInfoDTO> validatedFiles = Collections.emptyList();
        if (!imgIds.isEmpty()) {
            Result<List<FileInfoDTO>> fr = fileFeignClient.listInfos(imgIds);
            if (fr == null || !fr.isSuccess() || fr.getData() == null || fr.getData().size() != imgIds.size()) {
                throw new BusinessException("图片文件无效或已删除");
            }
            validatedFiles = fr.getData();
            for (FileInfoDTO f : validatedFiles) {
                if (f.getUserId() == null || !f.getUserId().equals(userId)) {
                    throw new BusinessException("仅可使用本人上传的图片");
                }
                if (f.getMimeType() == null || !f.getMimeType().startsWith("image/")) {
                    throw new BusinessException("评论仅支持图片附件");
                }
                if (f.getSize() != null) {
                    imgBytes += f.getSize();
                }
            }
            if (imgBytes > COMMENT_MEDIA_MAX) {
                throw new BusinessException("评论配图总大小不能超过 20MB");
            }
        }

        // 计算楼层号
        Long floor = commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>().eq(Comment::getPostId, dto.getPostId())) + 1;

        Comment comment = new Comment();
        comment.setPostId(dto.getPostId());
        comment.setUserId(userId);
        comment.setContent(text);
        comment.setContentMode(imgIds.isEmpty() ? 0 : 1);
        comment.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        comment.setReplyUid(dto.getReplyUid() != null ? dto.getReplyUid() : 0L);
        comment.setLikeCount(0);
        comment.setStatus(CommonConstant.COMMENT_STATUS_NORMAL);
        comment.setFloor(floor.intValue());
        commentMapper.insert(comment);

        if (!sensitiveHits.isEmpty()) {
            sensitiveFilterRecordService.saveCommentRecord(
                    comment.getCommentId(),
                    userId,
                    ContextHolder.getUsername(),
                    originalText,
                    text,
                    sensitiveHits
            );
        }

        Map<Long, FileInfoDTO> byFid = new HashMap<>();
        for (FileInfoDTO f : validatedFiles) {
            if (f.getId() != null) {
                byFid.put(Long.parseLong(f.getId().trim()), f);
            }
        }
        int sort = 0;
        for (Long fid : imgIds) {
            FileInfoDTO meta = byFid.get(fid);
            CommentMedia cm = new CommentMedia();
            cm.setCommentId(comment.getCommentId());
            cm.setFileId(fid);
            cm.setMediaKind(1);
            cm.setSortOrder(sort++);
            cm.setSizeBytes(meta != null && meta.getSize() != null ? meta.getSize() : 0L);
            commentMediaMapper.insert(cm);
        }

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
            String preview = text.isEmpty() ? "[图片]" : (text.length() > 50 ? text.substring(0, 50) : text);
            msg.put("content", preview);
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

        try {
            List<CommentMedia> cms = commentMediaMapper.listByCommentId(comment.getCommentId());
            if (cms != null && !cms.isEmpty()) {
                List<String> urls = new ArrayList<>();
                for (CommentMedia cm : cms) {
                    if (cm.getFileId() != null) {
                        urls.add(FILE_DL + cm.getFileId());
                    }
                }
                vo.setImageUrls(urls);
            }
        } catch (Exception e) {
            log.debug("comment_media: {}", e.getMessage());
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

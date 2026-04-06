package com.community.community.service;

import cn.hutool.core.util.StrUtil;
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
import com.community.common.util.SensitiveWordFilter;
import com.community.community.dto.PostDTO;
import com.community.community.dto.PostVO;
import com.community.community.entity.*;
import com.community.community.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final SectionMapper sectionMapper;
    private final PostTopicMapper postTopicMapper;
    private final PostDraftMapper postDraftMapper;
    private final PostReportMapper postReportMapper;
    private final RedisUtil redisUtil;
    private final SensitiveWordFilter sensitiveWordFilter;
    private final RabbitTemplate rabbitTemplate;

    public PageResult<PostVO> listPosts(String keyword, Long sectionId, Long topicId,
                                         String sort, Integer pageNum, Integer pageSize) {
        Page<Post> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, CommonConstant.POST_STATUS_NORMAL);

        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Post::getTitle, keyword).or().like(Post::getContent, keyword));
        }
        if (sectionId != null) {
            wrapper.eq(Post::getSectionId, sectionId);
        }
        if (topicId != null) {
            // 通过话题关联表查询
            List<Long> postIds = postTopicMapper.selectList(
                    new LambdaQueryWrapper<PostTopic>().eq(PostTopic::getTopicId, topicId))
                    .stream().map(PostTopic::getPostId).collect(Collectors.toList());
            if (postIds.isEmpty()) {
                return new PageResult<>(Collections.emptyList(), 0L, pageSize, pageNum);
            }
            wrapper.in(Post::getPostId, postIds);
        }

        // 排序: hot/latest/top/essence
        switch (sort) {
            case "hot" -> wrapper.orderByDesc(Post::getLikeCount)
                    .orderByDesc(Post::getCommentCount).orderByDesc(Post::getViewCount);
            case "top" -> wrapper.eq(Post::getIsTop, 1).orderByDesc(Post::getCreateTime);
            case "essence" -> wrapper.eq(Post::getIsEssence, 1).orderByDesc(Post::getCreateTime);
            default -> wrapper.orderByDesc(Post::getIsTop)
                    .orderByDesc(Post::getIsEssence).orderByDesc(Post::getCreateTime);
        }
        Page<Post> result = postMapper.selectPage(page, wrapper);

        List<PostVO> voList = result.getRecords().stream().map(post -> convertToVO(post, null)).collect(Collectors.toList());
        return new PageResult<>(voList, result.getTotal(), result.getSize(), result.getCurrent());
    }

    public PageResult<PostVO> listUserPosts(Long userId, Integer pageNum, Integer pageSize) {
        Page<Post> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getUserId, userId)
               .eq(Post::getStatus, CommonConstant.POST_STATUS_NORMAL)
               .orderByDesc(Post::getCreateTime);
        Page<Post> result = postMapper.selectPage(page, wrapper);
        List<PostVO> voList = result.getRecords().stream()
                .map(p -> convertToVO(p, null)).collect(Collectors.toList());
        return new PageResult<>(voList, result.getTotal(), result.getSize(), result.getCurrent());
    }

    @Transactional
    @OperationLog(module = "帖子", operation = "发布帖子")
    public Long createPost(Long userId, PostDTO dto) {
        Section section = sectionMapper.selectById(dto.getSectionId());
        if (section == null || section.getStatus() != 1) {
            throw new BusinessException(ResultCode.SECTION_NOT_FOUND);
        }

        // 敏感词检测
        if (sensitiveWordFilter.containsSensitiveWord(dto.getTitle() + dto.getContent())) {
            throw new BusinessException(ResultCode.SENSITIVE_WORD_FOUND);
        }

        Post post = new Post();
        post.setUserId(userId);
        post.setSectionId(dto.getSectionId());
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setImages(dto.getImages());
        post.setVideo(dto.getVideo());
        post.setTopicIds(dto.getTopicIds());
        post.setTags(dto.getTags());
        post.setIsAnonymous(dto.getIsAnonymous() != null ? dto.getIsAnonymous() : 0);
        post.setIsTop(0);
        post.setIsEssence(0);
        post.setViewCount(0L);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCollectCount(0);
        post.setStatus(CommonConstant.POST_STATUS_NORMAL);
        post.setShareCount(0);
        postMapper.insert(post);

        // 处理话题关联
        if (StrUtil.isNotBlank(dto.getTopicIds())) {
            String[] topicArr = dto.getTopicIds().split(",");
            for (String tid : topicArr) {
                if (StrUtil.isNotBlank(tid)) {
                    PostTopic pt = new PostTopic();
                    pt.setPostId(post.getPostId());
                    pt.setTopicId(Long.parseLong(tid.trim()));
                    postTopicMapper.insert(pt);
                }
            }
        }

        // 同步到搜索服务
        Map<String, Object> searchMsg = new HashMap<>();
        searchMsg.put("postId", post.getPostId());
        searchMsg.put("title", dto.getTitle());
        searchMsg.put("content", dto.getContent());
        searchMsg.put("action", "create");
        rabbitTemplate.convertAndSend(MQConstant.COMMUNITY_EXCHANGE, MQConstant.SEARCH_SYNC_ROUTING_KEY, searchMsg);

        log.info("用户{}发布帖子: {}", userId, post.getPostId());
        return post.getPostId();
    }

    public PostVO getPostDetail(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getStatus() == CommonConstant.POST_STATUS_DELETED) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }
        // 增加浏览量
        redisUtil.increment(RedisKeyConstant.POST_VIEW_PREFIX + postId);
        return convertToVO(post, null);
    }

    @Transactional
    @OperationLog(module = "帖子", operation = "编辑帖子")
    public void updatePost(Long userId, Long postId, PostDTO dto) {
        Post post = postMapper.selectById(postId);
        if (post == null) throw new BusinessException(ResultCode.POST_NOT_FOUND);
        if (!post.getUserId().equals(userId)) throw new BusinessException("无权限修改此帖子");

        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setImages(dto.getImages());
        post.setVideo(dto.getVideo());
        post.setSectionId(dto.getSectionId());
        post.setTopicIds(dto.getTopicIds());
        post.setTags(dto.getTags());
        postMapper.updateById(post);

        // 更新话题关联
        postTopicMapper.delete(new LambdaQueryWrapper<PostTopic>().eq(PostTopic::getPostId, postId));
        if (StrUtil.isNotBlank(dto.getTopicIds())) {
            for (String tid : dto.getTopicIds().split(",")) {
                if (StrUtil.isNotBlank(tid.trim())) {
                    PostTopic pt = new PostTopic();
                    pt.setPostId(postId);
                    pt.setTopicId(Long.parseLong(tid.trim()));
                    postTopicMapper.insert(pt);
                }
            }
        }
    }

    @Transactional
    @OperationLog(module = "帖子", operation = "删除帖子")
    public void deletePost(Long userId, Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) throw new BusinessException(ResultCode.POST_NOT_FOUND);
        if (!post.getUserId().equals(userId)) throw new BusinessException("无权限删除此帖子");

        post.setStatus(CommonConstant.POST_STATUS_DELETED);
        postMapper.updateById(post);
    }

    @OperationLog(module = "帖子", operation = "置顶/取消置顶")
    public void toggleTop(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) throw new BusinessException(ResultCode.POST_NOT_FOUND);
        post.setIsTop(post.getIsTop() == 1 ? 0 : 1);
        postMapper.updateById(post);
    }

    @OperationLog(module = "帖子", operation = "设为/取消精华")
    public void toggleEssence(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) throw new BusinessException(ResultCode.POST_NOT_FOUND);
        post.setIsEssence(post.getIsEssence() == 1 ? 0 : 1);
        postMapper.updateById(post);
    }

    public PageResult<PostVO> listHotPosts(Integer pageNum, Integer pageSize) {
        Page<Post> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, CommonConstant.POST_STATUS_NORMAL)
              .and(w -> w.eq(Post::getIsTop, 1).or().eq(Post::getIsEssence, 1))
              .orderByDesc(Post::getLikeCount)
              .orderByDesc(Post::getCommentCount)
              .orderByDesc(Post::getViewCount);
        Page<Post> result = postMapper.selectPage(page, wrapper);
        List<PostVO> voList = result.getRecords().stream()
                .map(p -> convertToVO(p, null)).collect(Collectors.toList());
        return new PageResult<>(voList, result.getTotal(), result.getSize(), result.getCurrent());
    }

    // 草稿相关
    @Transactional
    public Long saveDraft(Long userId, PostDTO dto) {
        PostDraft draft = new PostDraft();
        draft.setUserId(userId);
        draft.setSectionId(dto.getSectionId());
        draft.setTitle(dto.getTitle());
        draft.setContent(dto.getContent());
        draft.setImages(dto.getImages());
        draft.setVideo(dto.getVideo());
        draft.setTopicIds(dto.getTopicIds());
        postDraftMapper.insert(draft);
        return draft.getDraftId();
    }

    public PageResult<PostDraft> listDrafts(Long userId, Integer pageNum, Integer pageSize) {
        Page<PostDraft> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PostDraft> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostDraft::getUserId, userId).orderByDesc(PostDraft::getUpdateTime);
        Page<PostDraft> result = postDraftMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getSize(), result.getCurrent());
    }

    public void deleteDraft(Long userId, Long draftId) {
        PostDraft draft = postDraftMapper.selectById(draftId);
        if (draft != null && draft.getUserId().equals(userId)) {
            postDraftMapper.deleteById(draftId);
        }
    }

    // 举报
    @Transactional
    public void reportPost(Long userId, Long postId, String reason) {
        Post post = postMapper.selectById(postId);
        if (post == null) throw new BusinessException(ResultCode.POST_NOT_FOUND);
        PostReport report = new PostReport();
        report.setPostId(postId);
        report.setUserId(userId);
        report.setReason(reason);
        report.setStatus(0);
        postReportMapper.insert(report);
    }

    private PostVO convertToVO(Post post, Boolean isLiked) {
        PostVO vo = new PostVO();
        vo.setPostId(post.getPostId());
        vo.setUserId(post.getUserId());
        vo.setAuthorId(post.getUserId());  // 作者ID
        vo.setSectionId(post.getSectionId());
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());
        vo.setImages(post.getImages());
        vo.setSummary(post.getSummary());
        vo.setTags(post.getTags());
        vo.setIsTop(post.getIsTop());
        vo.setIsEssence(post.getIsEssence());
        vo.setIsAnonymous(post.getIsAnonymous());
        vo.setViewCount(post.getViewCount());
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setCollectCount(post.getCollectCount());
        vo.setShareCount(post.getShareCount());
        vo.setCreateTime(post.getCreateTime());

        Section section = sectionMapper.selectById(post.getSectionId());
        if (section != null) vo.setSectionName(section.getName());

        // 获取作者信息
        if (post.getIsAnonymous() == 1) {
            vo.setAuthorName("匿名用户");
            vo.setAuthorAvatar("");
        } else {
            String userInfoJson = redisUtil.get(RedisKeyConstant.USER_INFO_PREFIX + post.getUserId());
            if (userInfoJson != null) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    java.util.Map<String, Object> userInfo = mapper.readValue(userInfoJson, java.util.Map.class);
                    vo.setAuthorName((String) userInfo.getOrDefault("nickname", "用户" + post.getUserId()));
                    vo.setAuthorAvatar((String) userInfo.getOrDefault("avatar", ""));
                } catch (Exception e) {
                    vo.setAuthorName("用户" + post.getUserId());
                    vo.setAuthorAvatar("");
                }
            } else {
                vo.setAuthorName("用户" + post.getUserId());
                vo.setAuthorAvatar("");
            }
        }

        vo.setIsLiked(isLiked);
        return vo;
    }
}

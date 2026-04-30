package com.community.community.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.annotation.OperationLog;
import com.community.common.constant.CommonConstant;
import com.community.common.constant.MQConstant;
import com.community.common.constant.MediaSizeConstant;
import com.community.common.constant.RedisKeyConstant;
import com.community.common.dto.FileInfoDTO;
import com.community.common.dto.PostPublishResultVO;
import com.community.common.exception.BusinessException;
import com.community.common.dto.AiContentCheckResult;
import com.community.common.feign.AiReviewFeignClient;
import com.community.common.feign.FileFeignClient;
import com.community.common.result.PageResult;
import com.community.common.result.Result;
import com.community.common.result.ResultCode;
import com.community.common.util.ContextHolder;
import com.community.common.util.RedisUtil;
import com.community.common.util.SensitiveWordFilter;
import com.community.community.dto.PostDTO;
import com.community.community.dto.PostDraftSaveDTO;
import com.community.community.dto.PostMediaItemDTO;
import com.community.community.dto.PostMediaVO;
import com.community.community.dto.PostVO;
import com.community.community.entity.*;
import com.community.community.mapper.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private static final String FILE_DL_PREFIX = "/api/file/download/";

    private final PostMapper postMapper;
    private final SectionMapper sectionMapper;
    private final SectionMemberMapper sectionMemberMapper;
    private final PostTopicMapper postTopicMapper;
    private final PostDraftMapper postDraftMapper;
    private final PostReportMapper postReportMapper;
    private final PostMediaMapper postMediaMapper;
    private final FileFeignClient fileFeignClient;
    private final RedisUtil redisUtil;
    private final SensitiveWordFilter sensitiveWordFilter;
    private final RabbitTemplate rabbitTemplate;
    private final AiReviewFeignClient aiReviewFeignClient;

    public PageResult<PostVO> listPosts(String keyword, Long sectionId, Long topicId,
                                         String sort, Integer pageNum, Integer pageSize, Long viewerUserId) {
        Page<Post> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, CommonConstant.POST_STATUS_NORMAL);
        // 私密帖子仅作者自己可见
        if (viewerUserId != null) {
            wrapper.and(w -> w.eq(Post::getIsPublic, 1)
                    .or()
                    .eq(Post::getUserId, viewerUserId));
        } else {
            wrapper.eq(Post::getIsPublic, 1);
        }

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

    public PageResult<PostVO> listUserPosts(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        Page<Post> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getUserId, userId);
        if (status != null) {
            wrapper.eq(Post::getStatus, status);
        } else {
            wrapper.in(Post::getStatus, CommonConstant.POST_STATUS_NORMAL,
                    CommonConstant.POST_STATUS_REVIEWING,
                    CommonConstant.POST_STATUS_REJECTED);
        }
        wrapper.orderByDesc(Post::getCreateTime);
        Page<Post> result = postMapper.selectPage(page, wrapper);
        List<PostVO> voList = result.getRecords().stream()
                .map(p -> convertToVO(p, null)).collect(Collectors.toList());
        return new PageResult<>(voList, result.getTotal(), result.getSize(), result.getCurrent());
    }

    /**
     * 推荐帖：优先根据用户近期发帖所在板块推荐，否则退化为热门帖
     */
    public PageResult<PostVO> listRecommended(Long userId, Integer pageNum, Integer pageSize) {
        if (userId == null) {
            return listHotPosts(pageNum, pageSize);
        }
        Page<Post> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, CommonConstant.POST_STATUS_NORMAL)
                .eq(Post::getIsPublic, 1)
                .ne(Post::getUserId, userId);

        List<Post> recent = postMapper.selectList(
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getUserId, userId)
                        .eq(Post::getStatus, CommonConstant.POST_STATUS_NORMAL)
                        .orderByDesc(Post::getCreateTime)
                        .last("LIMIT 30"));
        Set<Long> sectionIds = recent.stream()
                .map(Post::getSectionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (!sectionIds.isEmpty()) {
            wrapper.in(Post::getSectionId, sectionIds);
        }
        wrapper.orderByDesc(Post::getLikeCount)
                .orderByDesc(Post::getCommentCount)
                .orderByDesc(Post::getCreateTime);
        Page<Post> result = postMapper.selectPage(page, wrapper);
        if (result.getRecords().isEmpty()) {
            return listHotPosts(pageNum, pageSize);
        }
        List<PostVO> voList = result.getRecords().stream()
                .map(p -> convertToVO(p, null)).collect(Collectors.toList());
        return new PageResult<>(voList, result.getTotal(), result.getSize(), result.getCurrent());
    }

    private void assertCanPost(Section section, Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        if (section.getModeratorId() != null && section.getModeratorId().equals(userId)) {
            return;
        }
        if (section.getMemberOnlyPost() == null || section.getMemberOnlyPost() == 0) {
            return;
        }
        long n = sectionMemberMapper.selectCount(
                new LambdaQueryWrapper<SectionMember>()
                        .eq(SectionMember::getSectionId, section.getSectionId())
                        .eq(SectionMember::getUserId, userId)
                        .eq(SectionMember::getStatus, 1));
        if (n <= 0) {
            throw new BusinessException("本板块仅成员可发帖，请先申请加入并通过审核");
        }
    }

    /** 未指定板块时：取启用板块中 sort 最小的一条，保证发帖可不选板块 */
    private Long resolveDefaultSectionId() {
        Section s = sectionMapper.selectOne(
                new LambdaQueryWrapper<Section>()
                        .eq(Section::getStatus, 1)
                        .orderByAsc(Section::getSort)
                        .last("LIMIT 1"));
        if (s == null) {
            throw new BusinessException(ResultCode.SECTION_NOT_FOUND);
        }
        return s.getSectionId();
    }

    /** AI 审核结果 + 可选的 ai_check 主键（发帖/改帖成功后回写 content_id） */
    private static final class ModerationOutcome {
        final int status;
        final Long aiCheckRecordId;

        ModerationOutcome(int status, Long aiCheckRecordId) {
            this.status = status;
            this.aiCheckRecordId = aiCheckRecordId;
        }
    }

    /**
     * 本地敏感词 + 远程 AI 审核。返回 NORMAL / REVIEWING；违规则抛 {@link ResultCode#CONTENT_REVIEW_FAILED}
     */
    private ModerationOutcome moderateUserPostText(Long userId, String title, String content) {
        String t = title != null ? title : "";
        String c = content != null ? content : "";
        String full = t + c;
        if (sensitiveWordFilter.containsSensitiveWord(full)) {
            throw new BusinessException(ResultCode.SENSITIVE_WORD_FOUND);
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("userId", userId);
            body.put("content", full);
            body.put("type", 1);
            Result<AiContentCheckResult> r = aiReviewFeignClient.checkContent(body);
            if (r != null && r.isSuccess() && r.getData() != null && r.getData().getCheckResult() != null) {
                AiContentCheckResult data = r.getData();
                int cr = data.getCheckResult();
                Long recordId = data.getId();
                if (cr == 2) {
                    String msg = data.getAiSuggestion();
                    throw new BusinessException(ResultCode.CONTENT_REVIEW_FAILED,
                            msg != null ? msg : ResultCode.CONTENT_REVIEW_FAILED.getMessage());
                }
                if (cr == 1) {
                    return new ModerationOutcome(CommonConstant.POST_STATUS_REVIEWING, recordId);
                }
                return new ModerationOutcome(CommonConstant.POST_STATUS_NORMAL, recordId);
            }
            log.warn("AI 审核返回无效（success 或 checkResult 为空），帖子将进入待人工审核");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("AI 审核调用失败，不默认公开发帖，转待人工审核: {}", e.getMessage());
        }
        return new ModerationOutcome(CommonConstant.POST_STATUS_REVIEWING, null);
    }

    private void tryBindAiCheckToPost(Long checkRecordId, Long postId) {
        if (checkRecordId == null || postId == null) {
            return;
        }
        try {
            Result<Void> bind = aiReviewFeignClient.bindCheckToPost(checkRecordId, postId);
            if (bind == null || !bind.isSuccess()) {
                log.warn("绑定 AI 审核记录失败: checkId={} postId={} msg={}", checkRecordId, postId,
                        bind != null ? bind.getMessage() : "null");
            }
        } catch (Exception e) {
            log.warn("绑定 AI 审核记录异常 checkId={} postId={}: {}", checkRecordId, postId, e.getMessage());
        }
    }

    @Transactional
    @OperationLog(module = "帖子", operation = "发布帖子")
    public PostPublishResultVO createPost(Long userId, PostDTO dto) {
        Long sectionId = dto.getSectionId();
        if (sectionId == null) {
            sectionId = resolveDefaultSectionId();
        }
        Section section = sectionMapper.selectById(sectionId);
        if (section == null || section.getStatus() != 1) {
            throw new BusinessException(ResultCode.SECTION_NOT_FOUND);
        }
        assertCanPost(section, userId);

        ModerationOutcome moderation = moderateUserPostText(userId, dto.getTitle(), dto.getContent());
        int reviewStatus = moderation.status;

        Post post = new Post();
        post.setUserId(userId);
        post.setSectionId(sectionId);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setImages(dto.getImages());
        post.setCoverImage(dto.getCoverImage());
        post.setVideo(dto.getVideo());
        post.setTopicIds(dto.getTopicIds() != null && !dto.getTopicIds().isEmpty() ? String.join(",", dto.getTopicIds().stream().map(String::valueOf).collect(Collectors.toList())) : null);
        post.setTags(dto.getTags());
        post.setIsAnonymous(dto.getIsAnonymous() != null && dto.getIsAnonymous() ? 1 : 0);
        post.setIsPublic(dto.getIsPublic() != null && dto.getIsPublic() ? 1 : 0);
        post.setIsTop(0);
        post.setIsEssence(0);
        post.setViewCount(0L);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCollectCount(0);
        post.setStatus(reviewStatus);
        post.setShareCount(0);
        if (post.getTotalMediaBytes() == null) {
            post.setTotalMediaBytes(0L);
        }
        postMapper.insert(post);

        if (StringUtils.hasText(dto.getCoverFileId())) {
            post.setCoverFileId(parseSnowflakeFileId(dto.getCoverFileId(), "封面"));
        }
        if (dto.getMedia() != null) {
            applyPostMediaList(post, userId, dto.getMedia());
        }
        if (StringUtils.hasText(dto.getCoverFileId())) {
            applySingleCoverFile(post, userId, post.getCoverFileId());
        }
        if (dto.getMedia() != null || StringUtils.hasText(dto.getCoverFileId())) {
            postMapper.updateById(post);
        }

        // 处理话题关联
        if (dto.getTopicIds() != null && !dto.getTopicIds().isEmpty()) {
            for (Long topicId : dto.getTopicIds()) {
                PostTopic pt = new PostTopic();
                pt.setPostId(post.getPostId());
                pt.setTopicId(topicId);
                postTopicMapper.insert(pt);
            }
        }

        Long newPostId = post.getPostId();
        tryBindAiCheckToPost(moderation.aiCheckRecordId, newPostId);

        Post forSearch = postMapper.selectById(newPostId);
        if (forSearch != null && forSearch.getStatus() == CommonConstant.POST_STATUS_NORMAL) {
            sendSearchSyncMessage(forSearch, "upsert");
        }

        log.info("用户{}发布帖子: {}, 状态={}", userId, newPostId, reviewStatus);
        String tip = reviewStatus == CommonConstant.POST_STATUS_NORMAL
                ? "发布成功，帖子已公开"
                : "提交成功，内容已进入待人工审核，通过后将对所有人可见";
        return new PostPublishResultVO(newPostId, reviewStatus, tip);
    }

    public PostVO getPostDetail(Long postId) {
        return getPostDetail(postId, null);
    }

    /**
     * @param viewerUserId 当前访问者；待审/驳回贴仅作者本人可见
     */
    public PostVO getPostDetail(Long postId, Long viewerUserId) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getStatus() == CommonConstant.POST_STATUS_DELETED) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }
        if (post.getStatus() == CommonConstant.POST_STATUS_NORMAL) {
            redisUtil.increment(RedisKeyConstant.POST_VIEW_PREFIX + postId);
            return convertToVO(post, null);
        }
        if (viewerUserId != null && viewerUserId.equals(post.getUserId())
                && (post.getStatus() == CommonConstant.POST_STATUS_REVIEWING
                || post.getStatus() == CommonConstant.POST_STATUS_REJECTED)) {
            redisUtil.increment(RedisKeyConstant.POST_VIEW_PREFIX + postId);
            return convertToVO(post, null);
        }
        throw new BusinessException(ResultCode.POST_NOT_FOUND);
    }

    /** 管理端：非公开规则，可见非删除的各状态帖子 */
    public PostVO getPostDetailForAdmin(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getStatus() == CommonConstant.POST_STATUS_DELETED) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }
        return convertToVO(post, null);
    }

    public PageResult<PostVO> listPostsForAdmin(String keyword, Integer status, int pageNum, int pageSize) {
        Page<Post> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        if (status != null && status == CommonConstant.POST_STATUS_DELETED) {
            wrapper.eq(Post::getStatus, CommonConstant.POST_STATUS_DELETED);
        } else {
            wrapper.ne(Post::getStatus, CommonConstant.POST_STATUS_DELETED);
            if (status != null) {
                wrapper.eq(Post::getStatus, status);
            }
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Post::getTitle, keyword).or().like(Post::getContent, keyword));
        }
        wrapper.orderByDesc(Post::getCreateTime);
        Page<Post> result = postMapper.selectPage(page, wrapper);
        List<PostVO> voList = result.getRecords().stream()
                .map(p -> convertToVO(p, null)).collect(Collectors.toList());
        return new PageResult<>(voList, result.getTotal(), result.getSize(), result.getCurrent());
    }

    /**
     * 编辑仅更新本表字段（如 images、video、content）；不在此删除或覆盖历史 OSS 对象。
     * 新附件走 UUID 上传，旧资源与 GC 在定时任务中按全库引用收敛后删除。
     */
    @Transactional
    @OperationLog(module = "帖子", operation = "编辑帖子")
    public void updatePost(Long userId, Long postId, PostDTO dto) {
        Post post = postMapper.selectById(postId);
        if (post == null) throw new BusinessException(ResultCode.POST_NOT_FOUND);
        if (!post.getUserId().equals(userId)) throw new BusinessException("无权限修改此帖子");

        if (dto.getSectionId() != null && !dto.getSectionId().equals(post.getSectionId())) {
            Section newSec = sectionMapper.selectById(dto.getSectionId());
            if (newSec == null || newSec.getStatus() != 1) {
                throw new BusinessException(ResultCode.SECTION_NOT_FOUND);
            }
            assertCanPost(newSec, userId);
        }

        ModerationOutcome moderation = moderateUserPostText(userId, dto.getTitle(), dto.getContent());
        int reviewStatus = moderation.status;

        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        if (dto.getImages() != null) {
            post.setImages(dto.getImages());
        }
        if (dto.getCoverImage() != null) {
            post.setCoverImage(dto.getCoverImage());
        }
        if (dto.getVideo() != null) {
            post.setVideo(dto.getVideo());
        }
        if (dto.getSectionId() != null) {
            post.setSectionId(dto.getSectionId());
        }
        post.setTags(dto.getTags());
        if (dto.getIsAnonymous() != null) {
            post.setIsAnonymous(dto.getIsAnonymous() ? 1 : 0);
        }
        if (dto.getIsPublic() != null) {
            post.setIsPublic(dto.getIsPublic() ? 1 : 0);
        }
        if (dto.getTopicIds() != null) {
            post.setTopicIds(!dto.getTopicIds().isEmpty()
                    ? String.join(",", dto.getTopicIds().stream().map(String::valueOf).collect(Collectors.toList()))
                    : null);
        }
        if (StringUtils.hasText(dto.getCoverFileId())) {
            post.setCoverFileId(parseSnowflakeFileId(dto.getCoverFileId(), "封面"));
        }
        if (dto.getMedia() != null) {
            applyPostMediaList(post, userId, dto.getMedia());
        }
        if (StringUtils.hasText(dto.getCoverFileId())) {
            applySingleCoverFile(post, userId, post.getCoverFileId());
        }
        post.setStatus(reviewStatus);
        if (reviewStatus != CommonConstant.POST_STATUS_REJECTED) {
            post.setRejectReason(null);
        }
        postMapper.updateById(post);

        tryBindAiCheckToPost(moderation.aiCheckRecordId, postId);

        if (dto.getTopicIds() != null) {
            postTopicMapper.delete(new LambdaQueryWrapper<PostTopic>().eq(PostTopic::getPostId, postId));
            if (!dto.getTopicIds().isEmpty()) {
                for (Long topicId : dto.getTopicIds()) {
                    PostTopic pt = new PostTopic();
                    pt.setPostId(postId);
                    pt.setTopicId(topicId);
                    postTopicMapper.insert(pt);
                }
            }
        }

        Post refreshed = postMapper.selectById(postId);
        if (refreshed != null) {
            if (refreshed.getStatus() == CommonConstant.POST_STATUS_NORMAL) {
                sendSearchSyncMessage(refreshed, "upsert");
            } else {
                sendSearchSyncMessage(refreshed, "DELETE");
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
        sendSearchSyncMessage(post, "DELETE");
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
              .eq(Post::getIsPublic, 1)
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
    public Long saveDraft(Long userId, PostDraftSaveDTO dto) {
        if (dto.getDraftId() != null) {
            PostDraft existing = postDraftMapper.selectById(dto.getDraftId());
            if (existing == null || !existing.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.POST_NOT_FOUND);
            }
            if (dto.getSectionId() != null) {
                existing.setSectionId(dto.getSectionId());
            }
            if (dto.getTitle() != null) {
                existing.setTitle(dto.getTitle());
            }
            if (dto.getContent() != null) {
                existing.setContent(dto.getContent());
            }
            if (dto.getImages() != null) {
                existing.setImages(dto.getImages());
            }
            if (dto.getCoverImage() != null) {
                existing.setCoverImage(dto.getCoverImage());
            }
            if (dto.getVideo() != null) {
                existing.setVideo(dto.getVideo());
            }
            if (dto.getTopicIds() != null) {
                existing.setTopicIds(!dto.getTopicIds().isEmpty()
                        ? String.join(",", dto.getTopicIds().stream().map(String::valueOf).collect(Collectors.toList()))
                        : null);
            }
            if (dto.getIsAnonymous() != null) {
                existing.setIsAnonymous(dto.getIsAnonymous() ? 1 : 0);
            }
            postDraftMapper.updateById(existing);
            return existing.getDraftId();
        }
        PostDraft draft = new PostDraft();
        draft.setUserId(userId);
        draft.setSectionId(dto.getSectionId());
        draft.setTitle(dto.getTitle() != null ? dto.getTitle() : "");
        draft.setContent(dto.getContent() != null ? dto.getContent() : "");
        draft.setImages(dto.getImages());
        draft.setCoverImage(dto.getCoverImage());
        draft.setVideo(dto.getVideo());
        draft.setTopicIds(dto.getTopicIds() != null && !dto.getTopicIds().isEmpty()
                ? String.join(",", dto.getTopicIds().stream().map(String::valueOf).collect(Collectors.toList()))
                : null);
        draft.setIsAnonymous(dto.getIsAnonymous() != null && dto.getIsAnonymous() ? 1 : 0);
        postDraftMapper.insert(draft);
        return draft.getDraftId();
    }

    public PostDraft getDraft(Long userId, Long draftId) {
        PostDraft draft = postDraftMapper.selectById(draftId);
        if (draft == null || !draft.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }
        return draft;
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
    public void reportPost(Long userId, Long postId, String reason, String description) {
        Post post = postMapper.selectById(postId);
        if (post == null) throw new BusinessException(ResultCode.POST_NOT_FOUND);
        PostReport report = new PostReport();
        report.setPostId(postId);
        report.setUserId(userId);
        report.setReason(reason);
        report.setDescription(description);
        report.setStatus(0);
        postReportMapper.insert(report);
    }

    /**
     * 通过 RabbitMQ 同步帖子到搜索服务（Elasticsearch），解耦主库写入与索引更新
     */
    private void sendSearchSyncMessage(Post post, String action) {
        try {
            Map<String, Object> searchMsg = new HashMap<>();
            searchMsg.put("action", action);
            searchMsg.put("id", post.getPostId());
            if ("DELETE".equalsIgnoreCase(action)) {
                rabbitTemplate.convertAndSend(MQConstant.COMMUNITY_EXCHANGE, MQConstant.SEARCH_SYNC_ROUTING_KEY, searchMsg);
                return;
            }
            searchMsg.put("title", post.getTitle());
            searchMsg.put("content", post.getContent());
            searchMsg.put("summary", post.getSummary());
            searchMsg.put("authorId", post.getUserId());
            searchMsg.put("sectionId", post.getSectionId());
            searchMsg.put("status", post.getStatus());
            searchMsg.put("viewCount", post.getViewCount());
            searchMsg.put("likeCount", post.getLikeCount());
            searchMsg.put("createTime", post.getCreateTime());
            Section section = sectionMapper.selectById(post.getSectionId());
            searchMsg.put("sectionName", section != null ? section.getName() : "");
            String authorName = "匿名用户";
            if (post.getIsAnonymous() == null || post.getIsAnonymous() == 0) {
                String userInfoJson = redisUtil.get(RedisKeyConstant.USER_INFO_PREFIX + post.getUserId());
                if (userInfoJson != null) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        @SuppressWarnings("unchecked")
                        Map<String, Object> userInfo = mapper.readValue(userInfoJson, Map.class);
                        authorName = (String) userInfo.getOrDefault("nickname", "用户" + post.getUserId());
                    } catch (Exception e) {
                        authorName = "用户" + post.getUserId();
                    }
                } else {
                    authorName = "用户" + post.getUserId();
                }
            }
            searchMsg.put("authorName", authorName);
            rabbitTemplate.convertAndSend(MQConstant.COMMUNITY_EXCHANGE, MQConstant.SEARCH_SYNC_ROUTING_KEY, searchMsg);
        } catch (Exception e) {
            log.error("发送搜索同步消息失败", e);
        }
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
        vo.setCoverImage(post.getCoverImage());
        vo.setSummary(post.getSummary());
        vo.setTags(post.getTags());
        vo.setIsTop(post.getIsTop());
        vo.setIsEssence(post.getIsEssence());
        vo.setIsAnonymous(post.getIsAnonymous());
        vo.setIsPublic(post.getIsPublic());
        vo.setViewCount(post.getViewCount());
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setCollectCount(post.getCollectCount());
        vo.setShareCount(post.getShareCount());
        vo.setStatus(post.getStatus());
        vo.setRejectReason(post.getRejectReason());
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
        vo.setVideo(post.getVideo());
        vo.setCoverFileId(post.getCoverFileId());
        vo.setTotalMediaBytes(post.getTotalMediaBytes());
        try {
            List<PostMedia> pm = postMediaMapper.selectList(
                    new LambdaQueryWrapper<PostMedia>().eq(PostMedia::getPostId, post.getPostId())
                            .orderByAsc(PostMedia::getSortOrder));
            if (pm != null && !pm.isEmpty()) {
                List<PostMediaVO> ml = new ArrayList<>();
                for (PostMedia m : pm) {
                    PostMediaVO v = new PostMediaVO();
                    v.setFileId(m.getFileId() != null ? String.valueOf(m.getFileId()) : null);
                    v.setMediaType(m.getMediaType());
                    v.setSortOrder(m.getSortOrder());
                    v.setSizeBytes(m.getSizeBytes());
                    v.setDownloadPath(m.getFileId() != null ? FILE_DL_PREFIX + m.getFileId() : null);
                    ml.add(v);
                }
                vo.setMedia(ml);
            }
        } catch (Exception e) {
            log.debug("加载 post_media 失败: {}", e.getMessage());
        }
        return vo;
    }

    private void applySingleCoverFile(Post post, Long userId, Long coverFileId) {
        getFileInfoChecked(userId, coverFileId, true);
        post.setCoverFileId(coverFileId);
        post.setCoverImage(FILE_DL_PREFIX + coverFileId);
    }

    private Long parseSnowflakeFileId(String raw, String name) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String t = raw.trim();
        if (!t.matches("[0-9]+")) {
            throw new BusinessException("非法" + name + "文件ID");
        }
        try {
            return Long.parseLong(t);
        } catch (NumberFormatException e) {
            throw new BusinessException("非法" + name + "文件ID");
        }
    }

    private Long parseMediaItemFileId(PostMediaItemDTO item) {
        if (item.getFileId() == null) {
            return null;
        }
        return parseSnowflakeFileId(item.getFileId(), "媒资");
    }

    private FileInfoDTO getFileInfoChecked(Long userId, Long fileId, boolean imageOnly) {
        if (fileId == null) {
            throw new BusinessException("文件ID无效");
        }
        Result<List<FileInfoDTO>> r = fileFeignClient.listInfos(List.of(fileId));
        if (r == null || !r.isSuccess() || r.getData() == null || r.getData().isEmpty()) {
            throw new BusinessException("无法读取文件元数据");
        }
        FileInfoDTO f = r.getData().get(0);
        if (f.getUserId() == null || !f.getUserId().equals(userId)) {
            throw new BusinessException("仅可使用本人上传的附件");
        }
        if (imageOnly && f.getMimeType() != null && !f.getMimeType().startsWith("image/")) {
            throw new BusinessException("仅支持图片");
        }
        return f;
    }

    /**
     * 用客户端提交的 media 列表写 post_media 并回写 post.images、post.video、totalMediaBytes
     */
    private void applyPostMediaList(Post post, Long userId, List<PostMediaItemDTO> media) {
        if (post.getPostId() == null) {
            return;
        }
        if (media == null) {
            return;
        }
        if (media.isEmpty()) {
            postMediaMapper.delete(new LambdaQueryWrapper<PostMedia>().eq(PostMedia::getPostId, post.getPostId()));
            post.setImages("[]");
            post.setVideo("");
            if (post.getTotalMediaBytes() == null) {
                post.setTotalMediaBytes(0L);
            } else {
                post.setTotalMediaBytes(0L);
            }
            return;
        }
        List<PostMediaItemDTO> sorted = new ArrayList<>(media);
        sorted.sort(Comparator.comparingInt(a -> a.getSortOrder() != null ? a.getSortOrder() : 0));
        List<Long> ids = sorted.stream()
                .map(this::parseMediaItemFileId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (ids.isEmpty() && !sorted.isEmpty()) {
            throw new BusinessException("请填写有效的媒资 fileId");
        }
        Result<List<FileInfoDTO>> res = fileFeignClient.listInfos(ids);
        if (res == null || !res.isSuccess() || res.getData() == null) {
            throw new BusinessException("无法校验上传文件，请重试");
        }
        Map<Long, FileInfoDTO> byId = new HashMap<>();
        for (FileInfoDTO fi : res.getData()) {
            if (fi.getId() != null) {
                byId.put(Long.parseLong(fi.getId().trim()), fi);
            }
        }
        long total = 0;
        for (Long id : ids) {
            FileInfoDTO fi = byId.get(id);
            if (fi == null) {
                throw new BusinessException("文件不存在或已删除: " + id);
            }
            if (fi.getUserId() == null || !fi.getUserId().equals(userId)) {
                throw new BusinessException("仅可使用本人上传的附件");
            }
            if (fi.getSize() != null) {
                total += fi.getSize();
            }
        }
        if (post.getCoverFileId() != null && !ids.contains(post.getCoverFileId())) {
            Result<List<FileInfoDTO>> cr = fileFeignClient.listInfos(List.of(post.getCoverFileId()));
            if (cr != null && cr.isSuccess() && cr.getData() != null && !cr.getData().isEmpty()) {
                FileInfoDTO cfi = cr.getData().get(0);
                if (cfi.getSize() != null) {
                    total += cfi.getSize();
                }
            }
        }
        if (total > MediaSizeConstant.MAX_BYTES) {
            throw new BusinessException("本帖图、视频、封面总大小不能超过 100MB");
        }
        List<String> imageUrls = new ArrayList<>();
        String firstVideo = null;
        postMediaMapper.delete(new LambdaQueryWrapper<PostMedia>().eq(PostMedia::getPostId, post.getPostId()));
        int idx = 0;
        for (PostMediaItemDTO item : sorted) {
            Long fid = parseMediaItemFileId(item);
            if (fid == null) {
                continue;
            }
            int type = (item.getMediaType() != null && item.getMediaType() == 2) ? 2 : 1;
            FileInfoDTO fi = byId.get(fid);
            String path = FILE_DL_PREFIX + fid;
            if (type == 2) {
                if (firstVideo == null) {
                    firstVideo = path;
                }
            } else {
                imageUrls.add(path);
            }
            PostMedia row = new PostMedia();
            row.setPostId(post.getPostId());
            row.setFileId(fid);
            row.setMediaType(type);
            row.setSizeBytes(fi.getSize() != null ? fi.getSize() : 0L);
            row.setSortOrder(item.getSortOrder() != null ? item.getSortOrder() : idx);
            postMediaMapper.insert(row);
            idx++;
        }
        try {
            ObjectMapper om = new ObjectMapper();
            post.setImages(om.writeValueAsString(imageUrls));
        } catch (Exception e) {
            post.setImages("[]");
        }
        post.setVideo(firstVideo != null ? firstVideo : "");
        post.setTotalMediaBytes(total);
    }

    // ==================== 管理员方法 ====================

    /**
     * 人工审核帖子（管理员）：与 AI 待审（REVIEWING）、正式发布（NORMAL）、拒审（REJECTED）形成闭环。
     * <ul>
     *   <li>通过：仅允许从「待审核」或「已拒绝」→ NORMAL</li>
     *   <li>拒绝：允许从「待审核」或「已通过」→ REJECTED（下架），须填写原因</li>
     * </ul>
     */
    public void reviewPost(Long postId, Integer status, String reason) {
        Post post = postMapper.selectById(postId);
        if (post == null) throw new BusinessException(ResultCode.POST_NOT_FOUND);
        if (post.getStatus() == CommonConstant.POST_STATUS_DELETED) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "帖子已删除，无法审核");
        }
        if (status == null
                || (status != CommonConstant.POST_STATUS_NORMAL && status != CommonConstant.POST_STATUS_REJECTED)) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "审核结果仅支持通过(1)或拒绝(3)");
        }
        int cur = post.getStatus();
        if (status == CommonConstant.POST_STATUS_NORMAL) {
            if (cur != CommonConstant.POST_STATUS_REVIEWING && cur != CommonConstant.POST_STATUS_REJECTED) {
                throw new BusinessException(ResultCode.VALIDATE_FAILED,
                        "仅「待审核」或「已拒绝」的帖子可人工通过");
            }
        }
        if (status == CommonConstant.POST_STATUS_REJECTED) {
            if (cur != CommonConstant.POST_STATUS_REVIEWING && cur != CommonConstant.POST_STATUS_NORMAL) {
                throw new BusinessException(ResultCode.VALIDATE_FAILED,
                        "仅「待审核」或「已通过」的帖子可拒绝（含下架）");
            }
            if (reason == null || reason.isBlank()) {
                throw new BusinessException(ResultCode.VALIDATE_FAILED, "拒绝时请填写原因");
            }
        }

        post.setStatus(status);
        if (status == CommonConstant.POST_STATUS_REJECTED) {
            post.setRejectReason(reason.trim());
        } else {
            post.setRejectReason(null);
        }
        post.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(post);
        Post after = postMapper.selectById(postId);
        if (after != null) {
            if (after.getStatus() == CommonConstant.POST_STATUS_NORMAL) {
                sendSearchSyncMessage(after, "upsert");
            } else {
                sendSearchSyncMessage(after, "DELETE");
            }
        }
    }

    /**
     * 设置置顶（管理员权限）
     */
    public void setTop(Long postId, boolean isTop) {
        Post post = postMapper.selectById(postId);
        if (post == null) throw new BusinessException(ResultCode.POST_NOT_FOUND);
        
        post.setIsTop(isTop ? 1 : 0);
        post.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(post);
    }

    /**
     * 设置精华（管理员权限）
     */
    public void setEssence(Long postId, boolean isEssence) {
        Post post = postMapper.selectById(postId);
        if (post == null) throw new BusinessException(ResultCode.POST_NOT_FOUND);
        
        post.setIsEssence(isEssence ? 1 : 0);
        post.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(post);
    }

    /**
     * 删除帖子（管理员权限）
     */
    public void deletePost(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) throw new BusinessException(ResultCode.POST_NOT_FOUND);
        
        post.setStatus(CommonConstant.POST_STATUS_DELETED);
        post.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(post);
        sendSearchSyncMessage(post, "DELETE");
    }

    /**
     * 获取帖子数量
     */
    public long count() {
        return postMapper.selectCount(null);
    }
}

package com.community.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.constant.CommonConstant;
import com.community.common.exception.BusinessException;
import com.community.common.result.PageResult;
import com.community.common.result.ResultCode;
import com.community.common.util.ContextHolder;
import com.community.community.dto.SectionViewVO;
import com.community.community.entity.Post;
import com.community.community.entity.Section;
import com.community.community.entity.SectionCategory;
import com.community.community.entity.SectionJoinRequest;
import com.community.community.entity.SectionMember;
import com.community.community.mapper.PostMapper;
import com.community.community.mapper.SectionCategoryMapper;
import com.community.community.mapper.SectionJoinRequestMapper;
import com.community.community.mapper.SectionMapper;
import com.community.community.mapper.SectionMemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class SectionService {

    private final SectionMapper sectionMapper;
    private final SectionMemberMapper sectionMemberMapper;
    private final SectionJoinRequestMapper sectionJoinRequestMapper;
    private final SectionCategoryMapper sectionCategoryMapper;
    private final PostMapper postMapper;

    /**
     * 用帖子表真实统计覆盖 section.postCount（原字段可能未在发帖时回写，长期与库不一致）
     */
    private void fillPostCount(Section section) {
        if (section == null || section.getSectionId() == null) {
            return;
        }
        long c = postMapper.selectCount(
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getSectionId, section.getSectionId())
                        .eq(Post::getStatus, CommonConstant.POST_STATUS_NORMAL));
        section.setPostCount((int) Math.min(c, Integer.MAX_VALUE));
    }

    private void fillPostCountBatch(List<Section> sections) {
        if (sections == null) {
            return;
        }
        for (Section s : sections) {
            fillPostCount(s);
        }
    }

    public List<Section> listAllSections() {
        List<Section> list = sectionMapper.selectList(
                new LambdaQueryWrapper<Section>()
                        .eq(Section::getStatus, 1)
                        .orderByAsc(Section::getSort));
        fillPostCountBatch(list);
        return list;
    }

    public List<Section> listParentSections() {
        List<Section> list = sectionMapper.selectList(
                new LambdaQueryWrapper<Section>()
                        .eq(Section::getParentId, 0L)
                        .eq(Section::getStatus, 1)
                        .orderByAsc(Section::getSort));
        fillPostCountBatch(list);
        return list;
    }

    public Section getSectionDetail(Long sectionId) {
        Section section = sectionMapper.selectById(sectionId);
        if (section == null) throw new BusinessException(ResultCode.SECTION_NOT_FOUND);
        fillPostCount(section);
        return section;
    }

    public SectionViewVO getSectionView(Long sectionId, Long userId) {
        Section section = getSectionDetail(sectionId);
        long mc = sectionMemberMapper.selectCount(
                new LambdaQueryWrapper<SectionMember>()
                        .eq(SectionMember::getSectionId, sectionId)
                        .eq(SectionMember::getStatus, 1));
        // 版主未写入 section_member 的历史数据：成员数会显示 0，此处在统计中补全版主一人
        if (section.getModeratorId() != null) {
            long modIn = sectionMemberMapper.selectCount(
                    new LambdaQueryWrapper<SectionMember>()
                            .eq(SectionMember::getSectionId, sectionId)
                            .eq(SectionMember::getUserId, section.getModeratorId())
                            .eq(SectionMember::getStatus, 1));
            if (modIn == 0) {
                mc = mc + 1;
            }
        }

        boolean isMod = userId != null && section.getModeratorId() != null
                && section.getModeratorId().equals(userId);

        boolean isMember = isMod;
        if (userId != null && !isMember) {
            isMember = sectionMemberMapper.selectCount(
                    new LambdaQueryWrapper<SectionMember>()
                            .eq(SectionMember::getSectionId, sectionId)
                            .eq(SectionMember::getUserId, userId)
                            .eq(SectionMember::getStatus, 1)) > 0;
        }

        boolean joinPending = false;
        if (userId != null && !isMember && !isMod
                && section.getMemberOnlyPost() != null && section.getMemberOnlyPost() == 1) {
            SectionJoinRequest r = sectionJoinRequestMapper.selectOne(
                    new LambdaQueryWrapper<SectionJoinRequest>()
                            .eq(SectionJoinRequest::getSectionId, sectionId)
                            .eq(SectionJoinRequest::getUserId, userId));
            joinPending = r != null && r.getStatus() != null && r.getStatus() == 0;
        }

        boolean canPost = false;
        if (userId != null) {
            if (isMod) {
                canPost = true;
            } else if (section.getMemberOnlyPost() != null && section.getMemberOnlyPost() == 1) {
                canPost = isMember;
            } else {
                canPost = true;
            }
        }

        SectionViewVO vo = new SectionViewVO();
        vo.setSection(section);
        vo.setMemberCount(mc);
        vo.setModerator(isMod);
        vo.setMember(isMember);
        vo.setJoinPending(joinPending);
        vo.setCanPost(canPost);
        return vo;
    }

    @Transactional
    public Section createSection(String name, String intro, String icon, Integer status, Long categoryId, String customCategory) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED);
        }
        Long userId = ContextHolder.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        Section section = new Section();
        section.setName(name.trim());
        section.setIntro(intro);
        section.setCover(icon);
        section.setSort(0);
        section.setStatus(status != null ? status : 1);
        section.setParentId(0L);
        section.setPostCount(0);
        section.setPostPerm(1);
        section.setModeratorId(userId);
        section.setCreateTime(LocalDateTime.now());

        // 处理分类：如果选择了现有分类，则使用categoryId；如果提供了自定义分类，则使用customCategory
        if (categoryId != null && categoryId > 0) {
            // 验证分类是否存在
            SectionCategory category = sectionCategoryMapper.selectById(categoryId);
            if (category != null && category.getStatus() != null && category.getStatus() == 1) {
                section.setCategoryId(categoryId);
                section.setCategoryName(category.getName());
            }
        } else if (customCategory != null && !customCategory.trim().isEmpty()) {
            // 使用自定义分类
            section.setCategoryName(customCategory.trim());
            // 尝试查找或创建该自定义分类
            SectionCategory existing = sectionCategoryMapper.selectOne(
                    new LambdaQueryWrapper<SectionCategory>()
                            .eq(SectionCategory::getName, customCategory.trim()));
            if (existing != null) {
                section.setCategoryId(existing.getCategoryId());
            }
            // 如果不存在，暂时只保存分类名称，不自动创建分类记录（避免过多垃圾分类）
        }

        sectionMapper.insert(section);

        SectionMember sm = new SectionMember();
        sm.setSectionId(section.getSectionId());
        sm.setUserId(userId);
        sm.setRole(2);
        sm.setJoinTime(LocalDateTime.now());
        sm.setStatus(1);
        try {
            sectionMemberMapper.insert(sm);
        } catch (Exception e) {
            log.warn("写入板块成员失败: {}", e.getMessage());
        }
        return section;
    }

    public List<Section> listUserSections(Long userId) {
        List<Section> list = sectionMapper.selectList(
                new LambdaQueryWrapper<Section>()
                        .eq(Section::getModeratorId, userId)
                        .eq(Section::getStatus, 1)
                        .orderByDesc(Section::getCreateTime));
        fillPostCountBatch(list);
        return list;
    }

    public List<Section> listJoinedSections(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        try {
            List<SectionMember> members = sectionMemberMapper.selectList(
                    new LambdaQueryWrapper<SectionMember>()
                            .eq(SectionMember::getUserId, userId)
                            .eq(SectionMember::getStatus, 1));
            if (members.isEmpty()) {
                return new ArrayList<>();
            }
            List<Long> sectionIds = members.stream().map(SectionMember::getSectionId).collect(Collectors.toList());
            List<Section> list = sectionMapper.selectList(
                    new LambdaQueryWrapper<Section>()
                            .in(Section::getSectionId, sectionIds)
                            .eq(Section::getStatus, 1)
                            .orderByDesc(Section::getCreateTime));
            fillPostCountBatch(list);
            return list;
        } catch (Exception e) {
            log.warn("查询「我加入的板块」失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public PageResult<SectionMember> pageMembers(Long sectionId, int pageNum, int pageSize) {
        Page<SectionMember> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SectionMember> w = new LambdaQueryWrapper<SectionMember>()
                .eq(SectionMember::getSectionId, sectionId)
                .eq(SectionMember::getStatus, 1)
                .orderByDesc(SectionMember::getJoinTime);
        Page<SectionMember> res = sectionMemberMapper.selectPage(page, w);
        return new PageResult<>(res.getRecords(), res.getTotal(), res.getSize(), res.getCurrent());
    }

    @Transactional
    public void applyJoin(Long sectionId, Long userId) {
        Section section = getSectionDetail(sectionId);
        if (section.getMemberOnlyPost() == null || section.getMemberOnlyPost() == 0) {
            throw new BusinessException("本板块无需申请加入");
        }
        if (section.getModeratorId() != null && section.getModeratorId().equals(userId)) {
            throw new BusinessException("版主无需申请");
        }
        if (sectionMemberMapper.selectCount(
                new LambdaQueryWrapper<SectionMember>()
                        .eq(SectionMember::getSectionId, sectionId)
                        .eq(SectionMember::getUserId, userId)
                        .eq(SectionMember::getStatus, 1)) > 0) {
            throw new BusinessException("已是板块成员");
        }
        SectionJoinRequest exist = sectionJoinRequestMapper.selectOne(
                new LambdaQueryWrapper<SectionJoinRequest>()
                        .eq(SectionJoinRequest::getSectionId, sectionId)
                        .eq(SectionJoinRequest::getUserId, userId));
        if (exist != null) {
            if (exist.getStatus() == 0) {
                throw new BusinessException("已提交申请，请等待版主审核");
            }
            if (exist.getStatus() == 1) {
                throw new BusinessException("申请已通过");
            }
            exist.setStatus(0);
            exist.setCreateTime(LocalDateTime.now());
            sectionJoinRequestMapper.updateById(exist);
            return;
        }
        SectionJoinRequest req = new SectionJoinRequest();
        req.setSectionId(sectionId);
        req.setUserId(userId);
        req.setStatus(0);
        req.setCreateTime(LocalDateTime.now());
        sectionJoinRequestMapper.insert(req);
    }

    public List<SectionJoinRequest> listPendingJoinRequests(Long sectionId, Long moderatorUserId) {
        Section section = getSectionDetail(sectionId);
        assertModerator(section, moderatorUserId);
        return sectionJoinRequestMapper.selectList(
                new LambdaQueryWrapper<SectionJoinRequest>()
                        .eq(SectionJoinRequest::getSectionId, sectionId)
                        .eq(SectionJoinRequest::getStatus, 0)
                        .orderByAsc(SectionJoinRequest::getCreateTime));
    }

    @Transactional
    public void approveJoin(Long sectionId, Long applicantUserId, Long moderatorUserId) {
        Section section = getSectionDetail(sectionId);
        assertModerator(section, moderatorUserId);
        SectionJoinRequest req = sectionJoinRequestMapper.selectOne(
                new LambdaQueryWrapper<SectionJoinRequest>()
                        .eq(SectionJoinRequest::getSectionId, sectionId)
                        .eq(SectionJoinRequest::getUserId, applicantUserId)
                        .eq(SectionJoinRequest::getStatus, 0));
        if (req == null) {
            throw new BusinessException("无待审申请");
        }
        req.setStatus(1);
        sectionJoinRequestMapper.updateById(req);

        long cnt = sectionMemberMapper.selectCount(
                new LambdaQueryWrapper<SectionMember>()
                        .eq(SectionMember::getSectionId, sectionId)
                        .eq(SectionMember::getUserId, applicantUserId));
        if (cnt == 0) {
            SectionMember m = new SectionMember();
            m.setSectionId(sectionId);
            m.setUserId(applicantUserId);
            m.setRole(0);
            m.setJoinTime(LocalDateTime.now());
            m.setStatus(1);
            sectionMemberMapper.insert(m);
        }
    }

    @Transactional
    public void rejectJoin(Long sectionId, Long applicantUserId, Long moderatorUserId) {
        Section section = getSectionDetail(sectionId);
        assertModerator(section, moderatorUserId);
        SectionJoinRequest req = sectionJoinRequestMapper.selectOne(
                new LambdaQueryWrapper<SectionJoinRequest>()
                        .eq(SectionJoinRequest::getSectionId, sectionId)
                        .eq(SectionJoinRequest::getUserId, applicantUserId)
                        .eq(SectionJoinRequest::getStatus, 0));
        if (req == null) {
            throw new BusinessException("无待审申请");
        }
        req.setStatus(2);
        sectionJoinRequestMapper.updateById(req);
    }

    @Transactional
    public void kickMember(Long sectionId, Long targetUserId, Long moderatorUserId) {
        Section section = getSectionDetail(sectionId);
        assertModerator(section, moderatorUserId);
        if (section.getModeratorId() != null && section.getModeratorId().equals(targetUserId)) {
            throw new BusinessException("不能移出版主");
        }
        sectionMemberMapper.delete(
                new LambdaQueryWrapper<SectionMember>()
                        .eq(SectionMember::getSectionId, sectionId)
                        .eq(SectionMember::getUserId, targetUserId));
    }

    private void assertModerator(Section section, Long userId) {
        if (userId == null || section.getModeratorId() == null || !section.getModeratorId().equals(userId)) {
            throw new BusinessException("仅版主可操作");
        }
    }

    /**
     * 获取所有启用的板块分类列表
     */
    public List<SectionCategory> listAllCategories() {
        return sectionCategoryMapper.selectList(
                new LambdaQueryWrapper<SectionCategory>()
                        .eq(SectionCategory::getStatus, 1)
                        .orderByAsc(SectionCategory::getSort));
    }
}

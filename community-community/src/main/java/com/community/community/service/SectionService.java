package com.community.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.exception.BusinessException;
import com.community.common.result.ResultCode;
import com.community.common.util.ContextHolder;
import com.community.community.entity.Section;
import com.community.community.mapper.SectionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SectionService {

    private final SectionMapper sectionMapper;

    public List<Section> listAllSections() {
        return sectionMapper.selectList(
                new LambdaQueryWrapper<Section>()
                        .eq(Section::getStatus, 1)
                        .orderByAsc(Section::getSort));
    }

    public List<Section> listParentSections() {
        return sectionMapper.selectList(
                new LambdaQueryWrapper<Section>()
                        .eq(Section::getParentId, 0L)
                        .eq(Section::getStatus, 1)
                        .orderByAsc(Section::getSort));
    }

    public Section getSectionDetail(Long sectionId) {
        Section section = sectionMapper.selectById(sectionId);
        if (section == null) throw new BusinessException(ResultCode.SECTION_NOT_FOUND);
        return section;
    }

    public Section createSection(String name, String intro, String icon) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED);
        }
        Section section = new Section();
        section.setName(name.trim());
        section.setIntro(intro);
        section.setCover(icon);
        section.setSort(0);
        section.setStatus(1);
        section.setParentId(0L);
        section.setPostCount(0);
        section.setPostPerm(1);
        Long userId = ContextHolder.getUserId();
        if (userId != null) {
            section.setModeratorId(userId);
        }
        section.setCreateTime(LocalDateTime.now());
        sectionMapper.insert(section);
        return section;
    }
}

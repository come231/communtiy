package com.community.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.exception.BusinessException;
import com.community.common.result.ResultCode;
import com.community.community.entity.Section;
import com.community.community.mapper.SectionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}

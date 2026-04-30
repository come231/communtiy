package com.community.community.dto;

import com.community.community.entity.Section;
import lombok.Data;

@Data
public class SectionViewVO {
    private Section section;
    private long memberCount;
    /** 当前用户是否为版主 */
    private boolean moderator;
    /** 当前用户是否为已加入成员（含版主在成员表中的情况） */
    private boolean member;
    private boolean joinPending;
    private boolean canPost;
}

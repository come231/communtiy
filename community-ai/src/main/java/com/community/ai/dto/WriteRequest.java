package com.community.ai.dto;

import lombok.Data;

@Data
public class WriteRequest {

    /** 文章标题（写作时使用） */
    private String title;

    /** 写作要求/内容输入 */
    private String requirements;

    /** 待润色/翻译/总结的内容 */
    private String content;

    /** 目标语言（翻译时使用） */
    private String targetLang;

    /** 社区板块名称（如邻里互助、闲置交易） */
    private String boardName;

    /** 核心关键词或主题 */
    private String keyword;

    /** 期望字数范围（如50-100字、100-200字） */
    private String lengthRange;

    /** 风格（活泼、简洁、亲切、正式） */
    private String style;

    /** 指定段落编号（用于精准润色） */
    private Integer paragraphNo;

    /** 优化方向（语气调整、段落优化、合规优化、亮点优化） */
    private String polishMode;

    /** 用户已输入前缀（用于补全） */
    private String prefix;

    /** 当前帖子标题（用于摘要/回复建议） */
    private String postTitle;

    /** 反馈类型：like/dislike */
    private String feedbackType;

    /** 反馈原因：太啰嗦/不够亲切/信息不全等 */
    private String feedbackReason;

    /** 反馈场景：create/edit/detail/chat */
    private String scene;

    /** 触发的AI操作：generate/polish/summary等 */
    private String operation;
}

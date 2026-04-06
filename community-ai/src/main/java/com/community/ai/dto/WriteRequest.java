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
}

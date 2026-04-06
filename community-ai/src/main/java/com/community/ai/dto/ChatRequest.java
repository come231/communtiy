package com.community.ai.dto;

import lombok.Data;

@Data
public class ChatRequest {

    /** 用户消息 */
    private String message;

    /** 会话ID，为空则新建会话 */
    private String sessionId;
}

package com.community.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    /** AI回复 */
    private String reply;

    /** 会话ID */
    private String sessionId;

    /** 消耗token数 */
    private Integer tokens;
}

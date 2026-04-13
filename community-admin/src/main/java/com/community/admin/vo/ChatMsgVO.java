package com.community.admin.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMsgVO {
    private Long id;
    private Long fromUserId;  // 发送者ID
    private Long toUserId;    // 接收者ID
    private String content;   // 消息内容
    private Boolean isRead;   // 是否已读
    private LocalDateTime createTime; // 发送时间
}
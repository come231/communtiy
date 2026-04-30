package com.community.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_msg")
public class ChatMsg {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发送者ID
     */
    private Long fromUserId;

    /**
     * 接收者ID
     */
    private Long toUserId;

    /**
     * 1=text 2=image 3=video 4=file 5=mixed
     */
    private Integer msgType;

    private String content;

    /**
     * 本条消息附件总字节
     */
    private Long totalAttachmentBytes;

    @TableField("is_read")
    private Boolean isRead;

    private LocalDateTime createTime;

    public ChatMsg() {
        this.isRead = false;
        this.msgType = 1;
        this.totalAttachmentBytes = 0L;
        this.createTime = LocalDateTime.now();
    }
}

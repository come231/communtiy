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

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发送者ID
     */
    private Long fromUserId;

    /**
     * 接收者ID
     */
    private Long toUserId;

    private String content;

    /**
     * 消息类型: 0-文本 1-图片
     */
    @TableField("msg_type")
    private Integer msgType;

    @TableField("is_read")
    private Boolean isRead;

    private LocalDateTime createTime;

    public ChatMsg() {
        this.isRead = false;
        this.msgType = 0;
        this.createTime = LocalDateTime.now();
    }
}

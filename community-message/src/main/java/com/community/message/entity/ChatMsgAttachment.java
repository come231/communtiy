package com.community.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_msg_attachment")
public class ChatMsgAttachment {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long msgId;
    private Long fileId;
    /**
     * 1=image 2=video 3=file
     */
    private Integer attType;
    private String originalName;
    private Long sizeBytes;
    @TableField("sort_order")
    private Integer sortOrder;
    private LocalDateTime createTime;
}

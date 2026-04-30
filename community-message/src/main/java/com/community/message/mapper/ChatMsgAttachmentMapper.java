package com.community.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.message.entity.ChatMsgAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChatMsgAttachmentMapper extends BaseMapper<ChatMsgAttachment> {

    @Select("SELECT DISTINCT file_id FROM chat_msg_attachment")
    List<Long> listAllFileIds();
}

package com.community.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.message.entity.ChatMsg;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMsgMapper extends BaseMapper<ChatMsg> {
}

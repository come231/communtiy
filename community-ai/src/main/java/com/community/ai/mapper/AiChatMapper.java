package com.community.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.ai.entity.AiChat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AiChatMapper extends BaseMapper<AiChat> {

    /**
     * 获取所有会话列表（带用户信息和最新消息）
     */
    @Select("SELECT DISTINCT ac.session_id, ac.user_id, " +
            "(SELECT content FROM ai_chat WHERE session_id = ac.session_id AND role = 'user' ORDER BY create_time DESC LIMIT 1) as last_message, " +
            "MAX(ac.create_time) as latest_time, " +
            "COUNT(*) as message_count " +
            "FROM ai_chat ac " +
            "GROUP BY ac.session_id, ac.user_id " +
            "ORDER BY latest_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<Map<String, Object>> selectAllSessionsWithUser(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 获取不重复会话总数
     */
    @Select("SELECT COUNT(DISTINCT session_id) FROM ai_chat")
    Long selectDistinctSessionCount();

    /**
     * 获取今日活跃会话数
     */
    @Select("SELECT COUNT(DISTINCT session_id) FROM ai_chat WHERE create_time >= CURDATE()")
    Long selectTodaySessionCount();

    /**
     * 获取N天内有聊天的用户数
     */
    @Select("SELECT COUNT(DISTINCT user_id) FROM ai_chat WHERE create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY)")
    Long selectActiveUserCount(@Param("days") int days);
}

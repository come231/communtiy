package com.community.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.community.entity.CommentMedia;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMediaMapper extends BaseMapper<CommentMedia> {

    @Select("SELECT * FROM comment_media WHERE comment_id = #{commentId} ORDER BY sort_order")
    List<CommentMedia> listByCommentId(Long commentId);
}

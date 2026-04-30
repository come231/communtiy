package com.community.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.user.entity.UserTag;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserTagMapper extends BaseMapper<UserTag> {

    List<String> selectTagsByUserId(Long userId);
}

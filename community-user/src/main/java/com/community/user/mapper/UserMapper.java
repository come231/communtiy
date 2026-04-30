package com.community.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT TRIM(avatar) FROM `user` WHERE avatar IS NOT NULL AND TRIM(avatar) REGEXP '^[0-9]{3,18}$'")
    List<String> listAvatarFileIdStrings();
}

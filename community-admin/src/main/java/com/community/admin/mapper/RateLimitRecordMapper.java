package com.community.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.admin.entity.RateLimitRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RateLimitRecordMapper extends BaseMapper<RateLimitRecord> {
}

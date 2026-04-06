package com.community.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.exception.BusinessException;
import com.community.common.result.PageResult;
import com.community.common.result.ResultCode;
import com.community.user.dto.PageDTO;
import com.community.user.entity.PointsRecord;
import com.community.user.entity.User;
import com.community.user.entity.UserCheckin;
import com.community.user.mapper.PointsRecordMapper;
import com.community.user.mapper.UserCheckinMapper;
import com.community.user.mapper.UserMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckinService {

    private final UserCheckinMapper userCheckinMapper;
    private final UserMapper userMapper;
    private final PointsRecordMapper pointsRecordMapper;

    private static final int CHECKIN_POINTS = 5;
    private static final int CONTINUOUS_BONUS = 2;

    @Transactional
    public Map<String, Object> checkin(Long userId) {
        LocalDate today = LocalDate.now();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 检查是否已签到
        Long count = userCheckinMapper.selectCount(
                new LambdaQueryWrapper<UserCheckin>()
                        .eq(UserCheckin::getUserId, userId)
                        .eq(UserCheckin::getDate, today));
        if (count > 0) {
            throw new BusinessException("今日已签到");
        }

        // 计算连续签到天数
        int continuousDays = 1;
        if (user.getLastCheckin() != null) {
            LocalDate lastDate = user.getLastCheckin();
            if (lastDate.equals(today.minusDays(1))) {
                continuousDays = user.getContinuousCheckin() + 1;
            } else if (lastDate.equals(today)) {
                throw new BusinessException("今日已签到");
            }
        }

        // 计算积分
        int points = CHECKIN_POINTS + (continuousDays > 1 ? CONTINUOUS_BONUS : 0);

        // 保存签到记录
        UserCheckin checkin = new UserCheckin();
        checkin.setUserId(userId);
        checkin.setDate(today);
        checkin.setPoints(points);
        userCheckinMapper.insert(checkin);

        // 更新用户信息
        user.setLastCheckin(today);
        user.setContinuousCheckin(continuousDays);
        user.setPoints(user.getPoints() + points);
        userMapper.updateById(user);

        // 记录积分
        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setType(1);
        record.setPoints(points);
        record.setDescription("每日签到");
        pointsRecordMapper.insert(record);

        Map<String, Object> result = new HashMap<>();
        result.put("points", points);
        result.put("continuousDays", continuousDays);
        result.put("totalPoints", user.getPoints());

        log.info("用户{}签到成功，连续{}天，获得{}积分", userId, continuousDays, points);
        return result;
    }

    public boolean isCheckinToday(Long userId) {
        return userCheckinMapper.selectCount(
                new LambdaQueryWrapper<UserCheckin>()
                        .eq(UserCheckin::getUserId, userId)
                        .eq(UserCheckin::getDate, LocalDate.now())) > 0;
    }

    public Map<String, Object> getCheckinStatus(Long userId) {
        User user = userMapper.selectById(userId);
        boolean todayCheckin = isCheckinToday(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("continuousDays", user != null ? user.getContinuousCheckin() : 0);
        result.put("todayCheckin", todayCheckin);
        result.put("lastCheckin", user != null ? user.getLastCheckin() : null);
        return result;
    }
}

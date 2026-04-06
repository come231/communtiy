package com.community.user.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.constant.CommonConstant;
import com.community.common.constant.RedisKeyConstant;
import com.community.common.exception.BusinessException;
import com.community.common.result.PageResult;
import com.community.common.result.ResultCode;
import com.community.common.util.ContextHolder;
import com.community.common.util.JwtUtil;
import com.community.common.util.RedisUtil;
import com.community.user.dto.*;
import com.community.user.entity.*;
import com.community.user.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserFollowMapper userFollowMapper;
    private final UserBlockMapper userBlockMapper;
    private final PointsRecordMapper pointsRecordMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final StringRedisTemplate stringRedisTemplate;
    private final RabbitTemplate rabbitTemplate;

    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        if (user.getStatus() == CommonConstant.USER_STATUS_DISABLED) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // 生成双令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole());
        String accessToken = jwtUtil.generateAccessToken(user.getUserId(), user.getUsername(), claims);
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());

        // 存储RefreshToken到Redis
        redisUtil.set(RedisKeyConstant.USER_REFRESH_TOKEN_PREFIX + user.getUserId(),
                refreshToken, 7, TimeUnit.DAYS);

        // 存储用户信息到Redis（供其他服务获取作者信息）
        Map<String, Object> userInfoMap = new HashMap<>();
        userInfoMap.put("userId", user.getUserId());
        userInfoMap.put("username", user.getUsername());
        userInfoMap.put("nickname", user.getNickname());
        userInfoMap.put("avatar", user.getAvatar());
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String userInfoJson = mapper.writeValueAsString(userInfoMap);
            redisUtil.set(RedisKeyConstant.USER_INFO_PREFIX + user.getUserId(), userInfoJson, 30, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("存储用户信息到Redis失败: {}", e.getMessage());
        }

        // 构造返回
        LoginVO vo = new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setRefreshToken(refreshToken);
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());

        log.info("用户登录成功: {}", user.getUsername());
        return vo;
    }

    @Transactional
    public void register(RegisterDTO dto) {
        // 检查用户名是否存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setNickname(dto.getNickname());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=" + dto.getUsername());
        user.setStatus(CommonConstant.USER_STATUS_NORMAL);
        user.setRole(0);
        user.setExp(0);
        user.setPoints(0);
        user.setPostCount(0);
        user.setCollectCount(0);
        user.setContinuousCheckin(0);
        userMapper.insert(user);

        log.info("用户注册成功: {}", user.getUsername());
    }

    public LoginVO refreshToken(String refreshToken) {
        try {
            Long userId = jwtUtil.getUserId(refreshToken);
            if (!jwtUtil.isRefreshToken(refreshToken)) {
                throw new BusinessException(ResultCode.TOKEN_INVALID);
            }

            // 验证Redis中的RefreshToken
            String storedToken = redisUtil.get(RedisKeyConstant.USER_REFRESH_TOKEN_PREFIX + userId);
            if (!refreshToken.equals(storedToken)) {
                throw new BusinessException(ResultCode.TOKEN_INVALID);
            }

            User user = userMapper.selectById(userId);
            if (user == null || user.getStatus() == CommonConstant.USER_STATUS_DISABLED) {
                throw new BusinessException(ResultCode.USER_NOT_FOUND);
            }

            Map<String, Object> claims = new HashMap<>();
            claims.put("role", user.getRole());
            String newAccessToken = jwtUtil.generateAccessToken(user.getUserId(), user.getUsername(), claims);
            String newRefreshToken = jwtUtil.generateRefreshToken(user.getUserId());

            redisUtil.set(RedisKeyConstant.USER_REFRESH_TOKEN_PREFIX + userId,
                    newRefreshToken, 7, TimeUnit.DAYS);

            LoginVO vo = new LoginVO();
            vo.setAccessToken(newAccessToken);
            vo.setRefreshToken(newRefreshToken);
            vo.setUserId(user.getUserId());
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
            vo.setRole(user.getRole());
            return vo;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
    }

    public void logout(Long userId, String token) {
        // 将token加入黑名单
        if (StrUtil.isNotBlank(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        redisUtil.set(RedisKeyConstant.USER_BLACKLIST_PREFIX + userId, token, 30, TimeUnit.MINUTES);
        // 删除RefreshToken
        redisUtil.delete(RedisKeyConstant.USER_REFRESH_TOKEN_PREFIX + userId);
    }

    public UserVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return convertToVO(user, null);
    }

    public UserVO getUserInfo(Long currentUserId, Long targetUserId) {
        User user = userMapper.selectById(targetUserId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        // 检查是否关注
        Boolean isFollowed = null;
        if (currentUserId != null && !currentUserId.equals(targetUserId)) {
            isFollowed = userFollowMapper.selectCount(
                    new LambdaQueryWrapper<UserFollow>()
                            .eq(UserFollow::getUserId, currentUserId)
                            .eq(UserFollow::getFollowId, targetUserId)
                            .eq(UserFollow::getStatus, CommonConstant.FOLLOW_STATUS_FOLLOW)
            ) > 0;
        }
        return convertToVO(user, isFollowed);
    }

    @Transactional
    public UserVO updateUserInfo(Long userId, UserUpdateDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (StrUtil.isNotBlank(dto.getNickname())) user.setNickname(dto.getNickname());
        if (StrUtil.isNotBlank(dto.getAvatar())) user.setAvatar(dto.getAvatar());
        if (StrUtil.isNotBlank(dto.getBackground())) user.setBackground(dto.getBackground());
        if (StrUtil.isNotBlank(dto.getSignature())) user.setSignature(dto.getSignature());
        if (dto.getGender() != null) user.setGender(dto.getGender());
        if (dto.getBirthday() != null) user.setBirthday(dto.getBirthday());
        userMapper.updateById(user);

        // 清除缓存
        redisUtil.delete(RedisKeyConstant.USER_INFO_PREFIX + userId);

        return convertToVO(user, null);
    }

    public PageResult<UserVO> listUsers(String keyword, Integer pageNum, Integer pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userMapper.selectPage(page, wrapper);
        List<UserVO> voList = result.getRecords().stream().map(u -> convertToVO(u, null)).toList();
        return new PageResult<>(voList, result.getTotal(), result.getSize(), result.getCurrent());
    }

    private UserVO convertToVO(User user, Boolean isFollowed) {
        UserVO vo = new UserVO();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setBackground(user.getBackground());
        vo.setGender(user.getGender());
        vo.setSignature(user.getSignature());
        vo.setBirthday(user.getBirthday());
        vo.setRole(user.getRole());
        vo.setExp(user.getExp());
        vo.setPoints(user.getPoints());
        vo.setPostCount(user.getPostCount());
        vo.setCollectCount(user.getCollectCount());
        vo.setContinuousCheckin(user.getContinuousCheckin());
        vo.setIsFollowed(isFollowed);

        // 统计关注数和粉丝数
        Long followingCount = userFollowMapper.selectCount(
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getUserId, user.getUserId())
                        .eq(UserFollow::getStatus, CommonConstant.FOLLOW_STATUS_FOLLOW));
        Long followerCount = userFollowMapper.selectCount(
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowId, user.getUserId())
                        .eq(UserFollow::getStatus, CommonConstant.FOLLOW_STATUS_FOLLOW));
        vo.setFollowingCount(followingCount);
        vo.setFollowerCount(followerCount);

        return vo;
    }
}

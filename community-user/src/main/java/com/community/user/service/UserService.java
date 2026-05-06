package com.community.user.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.constant.CommonConstant;
import com.community.common.constant.RedisKeyConstant;
import com.community.common.dto.UserLikedPostItemDTO;
import com.community.common.exception.BusinessException;
import com.community.common.feign.InteractLikeFeignClient;
import com.community.common.result.PageResult;
import com.community.common.result.Result;
import com.community.common.result.ResultCode;
import com.community.common.util.ContextHolder;
import com.community.common.util.JwtUtil;
import com.community.common.util.RedisUtil;
import com.community.user.dto.*;
import com.community.user.entity.*;
import com.community.user.mapper.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserFollowMapper userFollowMapper;
    private final UserBlockMapper userBlockMapper;
    private final UserTagMapper userTagMapper;
    private final InteractLikeFeignClient interactLikeFeignClient;
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
            ObjectMapper mapper = new ObjectMapper();
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

    public Map<String, Object> getUserInfoById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        Map<String, Object> map= new HashMap<>();
        map.put("nickname",user.getNickname());
        map.put("avatar",user.getAvatar());
        return map;
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
        vo.setStatus(user.getStatus());  // 设置用户状态
        vo.setRole(user.getRole());
        vo.setExp(user.getExp());
        vo.setPoints(user.getPoints());
        vo.setPostCount(user.getPostCount());
        vo.setCollectCount(user.getCollectCount());
        vo.setLikeCount(user.getLikeCount());
        vo.setHideLikes(user.getHideLikes());
        vo.setHideCollects(user.getHideCollects());
        vo.setBioTags(user.getBioTags());
        vo.setContinuousCheckin(user.getContinuousCheckin());
        vo.setMuteEndTime(user.getMuteEndTime());  // 设置禁言截止时间
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

    public Long count() {
        Long count=userMapper.selectCount(new LambdaQueryWrapper<>(User.class));
        return count;
    }

    /**
     * 今日活跃用户数（今日有更新的用户，近似活跃）
     */
    public Long countTodayActive() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(User::getUpdateTime, LocalDateTime.of(LocalDate.now(), LocalTime.MIN));
        return userMapper.selectCount(wrapper);
    }

    /**
     * 今日新增用户数
     */
    public Long countTodayNew() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(User::getCreateTime, LocalDateTime.of(LocalDate.now(), LocalTime.MIN));
        return userMapper.selectCount(wrapper);
    }

    /**
     * 管理员更新用户状态（禁用/启用）
     */
    public void updateStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    /**
     * 管理员更新用户角色
     */
    public void updateRole(Long userId, Integer role) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setRole(role);
        userMapper.updateById(user);
    }

    /**
     * 禁言用户
     */
    public void muteUser(Long userId, Integer minutes, String reason) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        // 计算禁言结束时间
        LocalDateTime muteEndTime = LocalDateTime.now().plusMinutes(minutes);
        user.setMuteEndTime(muteEndTime);
        userMapper.updateById(user);
        log.info("用户 {} 被禁言 {} 分钟，原因: {}", userId, minutes, reason);
    }

    /**
     * 解除禁言
     */
    public void unmuteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setMuteEndTime(null);
        userMapper.updateById(user);
        log.info("用户 {} 禁言已解除", userId);
    }

    /**
     * 管理员批量更新用户信息
     */
    public void adminUpdateUser(Long userId, Map<String, Object> userInfo) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 更新状态
        if (userInfo.containsKey("status")) {
            Integer status = ((Number) userInfo.get("status")).intValue();
            user.setStatus(status);
        }

        // 更新角色
        if (userInfo.containsKey("role")) {
            Integer role = ((Number) userInfo.get("role")).intValue();
            user.setRole(role);
        }

        // 更新经验值
        if (userInfo.containsKey("exp")) {
            Integer exp = ((Number) userInfo.get("exp")).intValue();
            user.setExp(exp);
        }

        // 更新积分
        if (userInfo.containsKey("points")) {
            Integer points = ((Number) userInfo.get("points")).intValue();
            user.setPoints(points);
        }

        // 更新昵称
        if (userInfo.containsKey("nickname")) {
            user.setNickname((String) userInfo.get("nickname"));
        }

        userMapper.updateById(user);
        log.info("管理员更新用户信息: userId={}, fields={}", userId, userInfo.keySet());
    }

    /**
     * 搜索用户（支持状态和角色筛选）- 用于管理员
     */
    public PageResult<UserVO> searchUsers(String keyword, Integer pageNum, Integer pageSize, Integer status, Integer role) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword));
        }

        // 状态筛选
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }

        // 角色筛选
        if (role != null) {
            wrapper.eq(User::getRole, role);
        }

        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userMapper.selectPage(page, wrapper);
        List<UserVO> voList = result.getRecords().stream().map(u -> convertToVO(u, null)).toList();
        return new PageResult<>(voList, result.getTotal(), result.getSize(), result.getCurrent());
    }

    /**
     * 更新用户获赞数（内部调用）
     */
    @Transactional
    public void updateLikeCount(Long userId, Integer delta) {
        if (userId == null || delta == null) return;
        User user = userMapper.selectById(userId);
        if (user == null) return;

        int newCount = Math.max(0, (user.getLikeCount() != null ? user.getLikeCount() : 0) + delta);
        user.setLikeCount(newCount);
        userMapper.updateById(user);
    }

    /**
     * 获取隐私设置
     */
    public Map<String, Object> getPrivacySettings(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        Map<String, Object> settings = new HashMap<>();
        settings.put("hideLikes", user.getHideLikes() != null && user.getHideLikes() == 1);
        settings.put("hideCollects", user.getHideCollects() != null && user.getHideCollects() == 1);
        return settings;
    }

    /**
     * 更新隐私设置
     */
    @Transactional
    public void updatePrivacySettings(Long userId, Map<String, Integer> settings) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (settings.containsKey("hideLikes")) {
            user.setHideLikes(settings.get("hideLikes") != null && settings.get("hideLikes") == 1 ? 1 : 0);
        }
        if (settings.containsKey("hideCollects")) {
            user.setHideCollects(settings.get("hideCollects") != null && settings.get("hideCollects") == 1 ? 1 : 0);
        }
        userMapper.updateById(user);
    }

    /**
     * 获取用户喜欢的帖子列表
     */
    public PageResult<Map<String, Object>> getUserLikedPosts(Long viewerId, Long userId, Integer pageNum, Integer pageSize) {
        if (userId == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED);
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 检查隐私设置：非本人访问时，如果用户隐藏了喜欢列表，则返回空
        boolean isOwner = viewerId != null && viewerId.equals(userId);
        if (!isOwner && user.getHideLikes() != null && user.getHideLikes() == 1) {
            return new PageResult<>(Collections.emptyList(), 0L, pageSize, pageNum);
        }

        Result<PageResult<UserLikedPostItemDTO>> feignResult =
                interactLikeFeignClient.pageUserPostLikes(userId, pageNum, pageSize);
        if (feignResult == null || !feignResult.isSuccess() || feignResult.getData() == null) {
            log.warn("拉取用户点赞帖子失败 userId={}, msg={}", userId,
                    feignResult != null ? feignResult.getMessage() : "null response");
            return new PageResult<>(Collections.emptyList(), 0L, pageSize, pageNum);
        }
        PageResult<UserLikedPostItemDTO> data = feignResult.getData();
        List<Map<String, Object>> records = data.getRecords().stream().map(like -> {
            Map<String, Object> map = new HashMap<>();
            map.put("postId", like.getPostId());
            map.put("likeTime", like.getLikeTime());
            return map;
        }).collect(Collectors.toList());

        return new PageResult<>(records, data.getTotal(), data.getSize(), data.getCurrent());
    }
}

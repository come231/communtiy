package com.community.admin.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.admin.dto.AdminLoginDTO;
import com.community.admin.entity.SysAdmin;
import com.community.admin.mapper.SysAdminMapper;
import com.community.common.exception.BusinessException;
import com.community.common.result.ResultCode;
import com.community.common.util.JwtUtil;
import com.community.common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAuthService implements UserDetailsService {

    private final SysAdminMapper sysAdminMapper;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    private static final String ADMIN_REFRESH_TOKEN_PREFIX = "admin:refresh:";
    private static final String ADMIN_TOKEN_BLACKLIST_PREFIX = "admin:blacklist:";

    @Value("${jwt.access-token-expire:900000}")
    private long adminAccessExpire;

    @Value("${jwt.refresh-token-expire:259200000}")
    private long adminRefreshExpire;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysAdmin admin = sysAdminMapper.selectOne(
                new LambdaQueryWrapper<SysAdmin>().eq(SysAdmin::getUsername, username));
        if (admin == null || admin.getStatus() == 0) {
            throw new UsernameNotFoundException("管理员不存在或已禁用");
        }
        return new User(admin.getUsername(), admin.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    /**
     * 管理员登录
     */
    public Map<String, Object> login(AdminLoginDTO loginDTO) {
        SysAdmin admin = sysAdminMapper.selectOne(
                new LambdaQueryWrapper<SysAdmin>().eq(SysAdmin::getUsername, loginDTO.getUsername()));

        if (admin == null) {
            throw new BusinessException(ResultCode.FAILED, "账号不存在");
        }

        if (admin.getStatus() == 0) {
            throw new BusinessException(ResultCode.FAILED, "账号已被禁用");
        }

        String md5Password = DigestUtil.md5Hex(loginDTO.getPassword());
        if (!md5Password.equals(admin.getPassword())) {
            throw new BusinessException(ResultCode.FAILED, "账号或密码错误");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ADMIN");
        claims.put("realName", admin.getRealName());

        String accessToken = jwtUtil.generateAccessToken(admin.getId(), admin.getUsername(), claims);
        String refreshToken = jwtUtil.generateRefreshToken(admin.getId());

        // 存储刷新令牌到Redis
        redisUtil.set(ADMIN_REFRESH_TOKEN_PREFIX + admin.getId(), refreshToken, adminRefreshExpire, TimeUnit.MILLISECONDS);

        log.info("管理员登录成功: {}", admin.getUsername());

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "adminId", admin.getId(),
                "username", admin.getUsername(),
                "realName", admin.getRealName(),
                "role", "ADMIN"
        );
    }

    /**
     * 管理员退出
     */
    public void logout(String accessToken, Long adminId) {
        if (accessToken != null && !accessToken.isEmpty()) {
            long remain = jwtUtil.getTokenRemainingTime(accessToken);
            if (remain > 0) {
                redisUtil.set(ADMIN_TOKEN_BLACKLIST_PREFIX + accessToken, "black", remain, TimeUnit.MILLISECONDS);
            }
        }
        if (adminId != null) {
            redisUtil.delete(ADMIN_REFRESH_TOKEN_PREFIX + adminId);
        }
        log.info("管理员退出: adminId={}", adminId);
    }

    /**
     * 刷新令牌
     */
    public Map<String, Object> refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new BusinessException(ResultCode.FAILED, "刷新令牌不能为空");
        }

        // 验证刷新令牌格式
        if (jwtUtil.isTokenExpired(refreshToken)) {
            throw new BusinessException(ResultCode.FAILED, "刷新令牌已过期");
        }

        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new BusinessException(ResultCode.FAILED, "无效的刷新令牌");
        }

        Long adminId = jwtUtil.getUserId(refreshToken);
        if (adminId == null) {
            throw new BusinessException(ResultCode.FAILED, "无效的刷新令牌");
        }

        // 验证Redis中的刷新令牌
        String storedToken = redisUtil.get(ADMIN_REFRESH_TOKEN_PREFIX + adminId);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new BusinessException(ResultCode.FAILED, "刷新令牌已失效，请重新登录");
        }

        // 获取管理员信息
        SysAdmin admin = sysAdminMapper.selectById(adminId);
        if (admin == null || admin.getStatus() == 0) {
            throw new BusinessException(ResultCode.FAILED, "账号不存在或已被禁用");
        }

        // 生成新的访问令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ADMIN");
        claims.put("realName", admin.getRealName());
        String newAccessToken = jwtUtil.generateAccessToken(admin.getId(), admin.getUsername(), claims);

        log.info("管理员刷新令牌成功: {}", admin.getUsername());

        return Map.of(
                "accessToken", newAccessToken,
                "refreshToken", refreshToken,
                "adminId", admin.getId(),
                "username", admin.getUsername(),
                "realName", admin.getRealName(),
                "role", "ADMIN"
        );
    }

    /**
     * 获取管理员信息
     */
    public Map<String, Object> getAdminInfo(Long adminId) {
        if (adminId == null) {
            throw new BusinessException(ResultCode.FAILED, "用户未登录");
        }
        SysAdmin admin = sysAdminMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(ResultCode.FAILED, "管理员不存在");
        }
        Map<String, Object> info = new HashMap<>();
        info.put("id", admin.getId());
        info.put("username", admin.getUsername());
        info.put("realName", admin.getRealName());
        info.put("avatar", admin.getAvatar());
        info.put("role", "ADMIN");
        return info;
    }

    /**
     * 检查令牌是否在黑名单
     */
    public boolean isBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        return Boolean.TRUE.equals(redisUtil.hasKey(ADMIN_TOKEN_BLACKLIST_PREFIX + token));
    }
}
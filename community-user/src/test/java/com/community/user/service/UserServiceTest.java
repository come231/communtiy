package com.community.user.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.community.common.constant.CommonConstant;
import com.community.common.exception.BusinessException;
import com.community.common.result.ResultCode;
import com.community.common.util.JwtUtil;
import com.community.common.util.RedisUtil;
import com.community.user.dto.LoginDTO;
import com.community.user.dto.LoginVO;
import com.community.user.dto.RegisterDTO;
import com.community.user.entity.User;
import com.community.user.mapper.PointsRecordMapper;
import com.community.user.mapper.UserBlockMapper;
import com.community.user.mapper.UserFollowMapper;
import com.community.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link UserService} 单元测试（纯 Mockito，不启动 Spring 容器、不连数据库）。
 * <p>
 * 关注点：登录/注册等业务分支、与持久化/缓存/JWT 的交互是否按预期调用（可用 {@link ArgumentCaptor} 进一步断言参数）。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 业务层单元测试")
class UserServiceTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private UserFollowMapper userFollowMapper;
    @Mock
    private UserBlockMapper userBlockMapper;
    @Mock
    private PointsRecordMapper pointsRecordMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private RedisUtil redisUtil;
    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private UserService userService;

    private User activeUser;

    @BeforeEach
    void setUp() {
        activeUser = new User();
        activeUser.setUserId(100L);
        activeUser.setUsername("alice");
        activeUser.setNickname("爱丽丝");
        activeUser.setAvatar("http://example.com/a.png");
        activeUser.setPassword("encoded-password");
        activeUser.setStatus(CommonConstant.USER_STATUS_NORMAL);
        activeUser.setRole(0);
    }

    @Nested
    @DisplayName("登录 login")
    class Login {

        @Test
        @DisplayName("用户名存在、密码正确、账号正常 → 返回双令牌并写入 Redis")
        void login_success() {
            LoginDTO dto = new LoginDTO();
            dto.setUsername("alice");
            dto.setPassword("plain");

            when(userMapper.selectOne(any(Wrapper.class))).thenReturn(activeUser);
            when(passwordEncoder.matches("plain", "encoded-password")).thenReturn(true);
            when(jwtUtil.generateAccessToken(eq(100L), eq("alice"), any())).thenReturn("access-jwt");
            when(jwtUtil.generateRefreshToken(100L)).thenReturn("refresh-jwt");

            LoginVO vo = userService.login(dto);

            assertThat(vo.getAccessToken()).isEqualTo("access-jwt");
            assertThat(vo.getRefreshToken()).isEqualTo("refresh-jwt");
            assertThat(vo.getUserId()).isEqualTo(100L);
            assertThat(vo.getUsername()).isEqualTo("alice");

            verify(redisUtil).set(any(String.class), eq("refresh-jwt"), eq(7L), eq(TimeUnit.DAYS));
        }

        @Test
        @DisplayName("用户不存在 → USER_NOT_FOUND")
        void login_userNotFound() {
            LoginDTO dto = new LoginDTO();
            dto.setUsername("nobody");
            dto.setPassword("x");
            when(userMapper.selectOne(any(Wrapper.class))).thenReturn(null);

            assertThatThrownBy(() -> userService.login(dto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(ResultCode.USER_NOT_FOUND.getCode());

            verify(passwordEncoder, never()).matches(any(), any());
        }

        @Test
        @DisplayName("密码错误 → PASSWORD_ERROR")
        void login_wrongPassword() {
            LoginDTO dto = new LoginDTO();
            dto.setUsername("alice");
            dto.setPassword("wrong");
            when(userMapper.selectOne(any(Wrapper.class))).thenReturn(activeUser);
            when(passwordEncoder.matches("wrong", "encoded-password")).thenReturn(false);

            assertThatThrownBy(() -> userService.login(dto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(ResultCode.PASSWORD_ERROR.getCode());
        }

        @Test
        @DisplayName("账号已禁用 → ACCOUNT_DISABLED")
        void login_disabled() {
            activeUser.setStatus(CommonConstant.USER_STATUS_DISABLED);
            LoginDTO dto = new LoginDTO();
            dto.setUsername("alice");
            dto.setPassword("plain");
            when(userMapper.selectOne(any(Wrapper.class))).thenReturn(activeUser);
            when(passwordEncoder.matches("plain", "encoded-password")).thenReturn(true);

            assertThatThrownBy(() -> userService.login(dto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(ResultCode.ACCOUNT_DISABLED.getCode());
        }
    }

    @Nested
    @DisplayName("注册 register")
    class Register {

        @Test
        @DisplayName("用户名未被占用 → insert 新用户")
        void register_success() {
            RegisterDTO dto = new RegisterDTO();
            dto.setUsername("newuser");
            dto.setPassword("secret12");
            dto.setNickname("新用户");

            when(userMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
            when(passwordEncoder.encode("secret12")).thenReturn("encoded-new");

            userService.register(dto);

            verify(userMapper).insert(any(User.class));
        }

        @Test
        @DisplayName("用户名已存在 → USER_ALREADY_EXISTS")
        void register_duplicate() {
            RegisterDTO dto = new RegisterDTO();
            dto.setUsername("dup");
            dto.setPassword("secret12");
            dto.setNickname("重复");
            when(userMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

            assertThatThrownBy(() -> userService.register(dto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(ResultCode.USER_ALREADY_EXISTS.getCode());

            verify(userMapper, never()).insert(any(User.class));
        }
    }
}

package com.community.user.controller;

import com.community.common.util.ContextHolder;
import com.community.common.util.JwtUtil;
import com.community.user.dto.LoginDTO;
import com.community.user.dto.LoginVO;
import com.community.user.dto.UserVO;
import com.community.user.service.CheckinService;
import com.community.user.service.FollowService;
import com.community.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link UserController} Web 层切片测试：只加载 MVC 与当前 Controller，其余 Bean 用 {@link MockBean} 替换。
 * <p>
 * 验证点：HTTP 映射、请求体反序列化、统一响应 {@link com.community.common.result.Result} 包装格式；
 * 不验证 Service 内部业务（应在 {@link com.community.user.service.UserServiceTest} 中覆盖）。
 * <p>
 * {@code addFilters = false}：关闭 Security 过滤器链，避免测试焦点偏离「接口契约」；安全规则若有单独需求可再增加专项测试。
 * <p>
 * {@link com.community.user.config.WebMvcConfig} 会注册 {@link com.community.user.interceptor.JwtInterceptor}，
 * 切片上下文中需为 JwtUtil、StringRedisTemplate 提供 Bean（此处用 {@link MockBean} 占位即可）。
 */
@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserController 控制层 WebMvc 测试")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /** 供 JwtInterceptor 注入；本测试不校验 JWT 逻辑 */
    @MockBean
    private JwtUtil jwtUtil;
    /** 供 JwtInterceptor 注入 */
    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @MockBean
    private UserService userService;
    @MockBean
    private FollowService followService;
    @MockBean
    private CheckinService checkinService;

    @AfterEach
    void tearDown() {
        ContextHolder.clear();
    }

    @Test
    @DisplayName("POST /user/login → 200，body 为 Result 包装 LoginVO")
    void login_ok() throws Exception {
        LoginVO vo = new LoginVO();
        vo.setAccessToken("at");
        vo.setRefreshToken("rt");
        vo.setUserId(1L);
        vo.setUsername("u");
        vo.setNickname("nick");
        vo.setAvatar("av");
        vo.setRole(0);
        when(userService.login(any(LoginDTO.class))).thenReturn(vo);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("u");
        dto.setPassword("p");

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("at"))
                .andExpect(jsonPath("$.data.refreshToken").value("rt"));

        verify(userService).login(any(LoginDTO.class));
    }

    @Test
    @DisplayName("GET /user/my → 使用 ContextHolder 中的当前用户 ID 调用 Service")
    void myInfo_usesContextHolder() throws Exception {
        UserVO userVO = new UserVO();
        userVO.setUserId(42L);
        userVO.setUsername("me");
        when(userService.getUserInfo(42L)).thenReturn(userVO);

        try (MockedStatic<ContextHolder> ctx = org.mockito.Mockito.mockStatic(ContextHolder.class)) {
            ctx.when(ContextHolder::getUserId).thenReturn(42L);

            mockMvc.perform(get("/user/my"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.userId").value(42));

            verify(userService).getUserInfo(eq(42L));
        }
    }
}

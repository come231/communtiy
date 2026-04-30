package com.community.user.mapper;

import com.baomidou.mybatisplus.test.autoconfigure.MybatisPlusTest;
import com.community.user.config.UserMapperTestConfiguration;
import com.community.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link UserMapper} 数据访问层切片测试：启动精简 Spring 上下文 + 嵌入式 H2，验证 MyBatis-Plus 与表映射是否正常。
 * <p>
 * 与 {@link com.community.user.service.UserServiceTest} 的区别：此处会真实执行 SQL（针对测试库），更贴近「SQL/映射是否正确」；
 * 业务分支仍以 Service 层单测为主，避免重复与维护成本过高。
 */
@MybatisPlusTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@Import(UserMapperTestConfiguration.class)
@Transactional
@DisplayName("UserMapper 数据访问层切片测试")
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    @DisplayName("insert 后可通过主键 selectById 读回")
    void insertAndSelectById() {
        User u = new User();
        u.setUsername("mapper_test_user");
        u.setNickname("测试");
        u.setPassword("x");
        u.setAvatar("http://example.com/x.png");
        u.setStatus(1);
        u.setRole(0);
        u.setExp(0);
        u.setPoints(0);
        u.setPostCount(0);
        u.setCollectCount(0);
        u.setContinuousCheckin(0);

        int rows = userMapper.insert(u);
        assertThat(rows).isEqualTo(1);
        assertThat(u.getUserId()).isNotNull();

        User loaded = userMapper.selectById(u.getUserId());
        assertThat(loaded).isNotNull();
        assertThat(loaded.getUsername()).isEqualTo("mapper_test_user");
        assertThat(loaded.getNickname()).isEqualTo("测试");
    }

    @Test
    @DisplayName("selectCount 可统计表中行数")
    void selectCount() {
        long before = userMapper.selectCount(null);
        User u = new User();
        u.setUsername("cnt_user");
        u.setNickname("c");
        u.setPassword("p");
        u.setAvatar("a");
        u.setStatus(1);
        u.setRole(0);
        u.setExp(0);
        u.setPoints(0);
        u.setPostCount(0);
        u.setCollectCount(0);
        u.setContinuousCheckin(0);
        userMapper.insert(u);
        long after = userMapper.selectCount(null);
        assertThat(after).isEqualTo(before + 1);
    }
}

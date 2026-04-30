package com.community.user.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

/**
 * Mapper 切片测试专用配置：只注册本模块 Mapper 扫描与 MyBatis-Plus 必要 Bean，
 * 避免引入 {@link com.community.common.config.MyBatisPlusConfig} 时与自动配置重复注册分页插件等。
 * <p>
 * H2 将 {@code USER} 视为关键字，直接 {@code INSERT INTO user} 会语法错误；测试库物理表使用 {@code t_user}，
 * 通过 {@link DynamicTableNameInnerInterceptor} 将逻辑表名 {@code user} 映射为 {@code t_user}，与生产库表名语义对齐。
 */
@TestConfiguration
@MapperScan("com.community.user.mapper")
public class UserMapperTestConfiguration {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        DynamicTableNameInnerInterceptor dynamic = new DynamicTableNameInnerInterceptor();
        dynamic.setTableNameHandler((sql, tableName) -> {
            if ("user".equalsIgnoreCase(tableName)) {
                return "t_user";
            }
            return tableName;
        });
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(dynamic);
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}

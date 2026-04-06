package com.community.admin.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    // ==================== 主数据源（管理员库）====================
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        return dataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    @Primary
    public JdbcTemplate primaryJdbcTemplate(@Qualifier("dataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    // ==================== 安全数据源 ====================
    @Bean
    @ConfigurationProperties("spring.security.datasource")
    public DataSourceProperties securityDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("securityDataSource")
    public DataSource securityDataSource() {
        return securityDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    public JdbcTemplate securityJdbcTemplate(@Qualifier("securityDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    // ==================== 内容数据源（帖子、评论等）====================
    @Bean
    @ConfigurationProperties("spring.content.datasource")
    public DataSourceProperties contentDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("contentDataSource")
    public DataSource contentDataSource() {
        return contentDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    public JdbcTemplate contentJdbcTemplate(@Qualifier("contentDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}

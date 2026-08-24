package com.innowise.identity_audit_compliance_service.audit.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class ClickHouseConfig {

    @Bean(name = "clickHouseDataSource")
    public DataSource clickHouseDataSource(
            @Value("${spring.datasource.clickhouse.url}") String url,
            @Value("${spring.datasource.clickhouse.username}") String username,
            @Value("${spring.datasource.clickhouse.password}") String password,
            @Value("${spring.datasource.clickhouse.driver-class-name}") String driver) {

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driver);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Bean(name = "clickHouseJdbcTemplate")
    public JdbcTemplate clickHouseJdbcTemplate(DataSource clickHouseDataSource) {
        return new JdbcTemplate(clickHouseDataSource);
    }
}
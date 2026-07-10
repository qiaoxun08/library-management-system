package com.library.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Testcontainers 配置基类
 *
 * 使用真实 MySQL 8.0 容器进行集成测试
 * 避免 H2 语法兼容问题掩盖 MyBatis SQL 问题
 *
 * 使用方式：测试类继承此基类即可
 */
@SpringBootTest
@Testcontainers
public abstract class TestcontainersConfig {

    @Container
    protected static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("library_test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("schema-mysql.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }
}

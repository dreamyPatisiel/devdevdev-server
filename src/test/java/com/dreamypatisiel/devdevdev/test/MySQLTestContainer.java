package com.dreamypatisiel.devdevdev.test;

import org.junit.jupiter.api.AfterAll;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * MySQL 테스트컨테이너를 제공하는 공통 클래스
 * 각 테스트 클래스가 독립적인 컨테이너를 사용하도록 변경(현재 사용X)
 */
@Testcontainers
public abstract class MySQLTestContainer {

    @Container
    @ServiceConnection
    protected static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("devdevdev_test")
            .withUsername("test")
            .withPassword("test")
            .withCommand(
                "--character-set-server=utf8mb4", 
                "--collation-server=utf8mb4_general_ci",
                "--ngram_token_size=1"
            );

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // 컨테이너 시작 확인
        if (!mysql.isRunning()) {
            mysql.start();
        }
        
        // 컨테이너 준비 대기
        waitForContainer();
        
        String jdbcUrl = mysql.getJdbcUrl();
        System.out.println("MySQL Container JDBC URL: " + jdbcUrl);
        
        registry.add("spring.datasource.url", () -> jdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MySQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        
        // HikariCP 완전 비활성화
        registry.add("spring.datasource.type", () -> "org.springframework.jdbc.datasource.SimpleDriverDataSource");
        
        // 로깅 레벨 설정
        registry.add("logging.level.com.zaxxer.hikari", () -> "OFF");
        registry.add("logging.level.com.zaxxer.hikari.pool", () -> "OFF");
        registry.add("logging.level.com.zaxxer.hikari.pool.PoolBase", () -> "OFF");
        registry.add("logging.level.com.zaxxer.hikari.pool.ProxyLeakTask", () -> "OFF");
    }
    
    private static void waitForContainer() {
        int maxRetries = 15;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                if (mysql.isRunning()) {
                    // 실제 커넥션 테스트
                    try (Connection conn = DriverManager.getConnection(
                            mysql.getJdbcUrl(), 
                            mysql.getUsername(), 
                            mysql.getPassword())) {
                        System.out.println("MySQL Container 연결 성공: " + mysql.getJdbcUrl());
                        return;
                    } catch (SQLException e) {
                        System.out.println("커넥션 대기 중... (" + retryCount + "/" + maxRetries + ")");
                    }
                }
                Thread.sleep(1000);
                retryCount++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        throw new RuntimeException("MySQL 컴테이너 연결 실패");
    }

    @AfterAll
    static void tearDownContainer() {
        // 컨테이너 종료
        if (mysql != null && mysql.isRunning()) {
            mysql.stop();
        }
    }
}

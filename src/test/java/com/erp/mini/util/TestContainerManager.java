package com.erp.mini.util;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

public class TestContainerManager {

    public static final MySQLContainer<?> MYSQL = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("testdb")
            .withUsername("root")
            .withPassword("password");

    public static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:latest"))
            .withExposedPorts(6379);

    private static volatile boolean mysqlStarted = false;

    public static void startMySQL() {
        if (!mysqlStarted) {
            synchronized (TestContainerManager.class) {
                if (!mysqlStarted) {
                    MYSQL.start();
                    mysqlStarted = true;
                }
            }
        }
    }

    public static void startRedis() {
        if (!REDIS.isRunning()) {
            REDIS.start();
        }
    }

    public static void registerMySQL(DynamicPropertyRegistry registry) {
        startMySQL();
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
    }

    public static void registerRedis(DynamicPropertyRegistry registry) {
        startRedis();
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    }
}

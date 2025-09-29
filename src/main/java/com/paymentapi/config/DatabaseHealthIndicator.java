package com.paymentapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;

@Component("db")
public class DatabaseHealthIndicator implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseHealthIndicator.class);
    private final DatabaseConfig databaseConfig;
    private final DataSource dataSource;

    public DatabaseHealthIndicator(DatabaseConfig databaseConfig, DataSource dataSource) {
        this.databaseConfig = databaseConfig;
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try {
            Instant start = Instant.now();

            boolean isValid = databaseConfig.isConnectionValid();

            Duration responseTime = Duration.between(start, Instant.now());

            if (isValid) {
                return Health.up()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("connectionValid", true)
                    .withDetail("responseTime", responseTime.toMillis() + "ms")
                    .withDetail("connectionPool", getConnectionPoolStatus())
                    .build();
            } else {
                return Health.down()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("connectionValid", false)
                    .withDetail("responseTime", responseTime.toMillis() + "ms")
                    .withDetail("error", "Database connection test failed")
                    .build();
            }

        } catch (Exception e) {
            logger.error("Database health check failed", e);
            return Health.down()
                .withDetail("database", "PostgreSQL")
                .withDetail("connectionValid", false)
                .withDetail("error", e.getMessage())
                .build();
        }
    }

    private String getConnectionPoolStatus() {
        try (Connection connection = dataSource.getConnection()) {
            return "Active connection obtained";
        } catch (SQLException e) {
            logger.warn("Failed to obtain connection for pool status check: {}", e.getMessage());
            return "Failed to obtain connection";
        }
    }
}
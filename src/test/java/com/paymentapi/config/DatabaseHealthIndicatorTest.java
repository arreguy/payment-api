package com.paymentapi.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseHealthIndicatorTest {

    @Mock
    private DatabaseConfig databaseConfig;

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    private DatabaseHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new DatabaseHealthIndicator(databaseConfig, dataSource);
    }

    @Test
    void health_WhenDatabaseConnectionValid_ShouldReturnUp() throws SQLException {
        // Arrange
        when(databaseConfig.isConnectionValid()).thenReturn(true);
        when(dataSource.getConnection()).thenReturn(connection);

        // Act
        Health health = healthIndicator.health();

        // Assert
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("database", "PostgreSQL");
        assertThat(health.getDetails()).containsEntry("connectionValid", true);
        assertThat(health.getDetails()).containsKey("responseTime");
        assertThat(health.getDetails()).containsKey("connectionPool");
    }

    @Test
    void health_WhenDatabaseConnectionInvalid_ShouldReturnDown() {
        // Arrange
        when(databaseConfig.isConnectionValid()).thenReturn(false);

        // Act
        Health health = healthIndicator.health();

        // Assert
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("database", "PostgreSQL");
        assertThat(health.getDetails()).containsEntry("connectionValid", false);
        assertThat(health.getDetails()).containsEntry("error", "Database connection test failed");
        assertThat(health.getDetails()).containsKey("responseTime");
    }

    @Test
    void health_WhenDatabaseConfigThrowsException_ShouldReturnDown() {
        // Arrange
        when(databaseConfig.isConnectionValid()).thenThrow(new RuntimeException("Database error"));

        // Act
        Health health = healthIndicator.health();

        // Assert
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("database", "PostgreSQL");
        assertThat(health.getDetails()).containsEntry("connectionValid", false);
        assertThat(health.getDetails()).containsEntry("error", "Database error");
    }

    @Test
    void health_WhenDataSourceConnectionFails_ShouldStillReturnValidHealthCheck() throws SQLException {
        // Arrange
        when(databaseConfig.isConnectionValid()).thenReturn(true);
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection pool exhausted"));

        // Act
        Health health = healthIndicator.health();

        // Assert
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("database", "PostgreSQL");
        assertThat(health.getDetails()).containsEntry("connectionValid", true);
        assertThat(health.getDetails()).containsKey("connectionPool");
    }
}
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

/**
 * Indicador de saúde do banco de dados.
 * Verifica se a conexão com o PostgreSQL está funcionando.
 */
@Component("db")
public class DatabaseHealthIndicator implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseHealthIndicator.class);
    private final DatabaseConfig databaseConfig;
    private final DataSource dataSource;

    public DatabaseHealthIndicator(DatabaseConfig databaseConfig, DataSource dataSource) {
        this.databaseConfig = databaseConfig;
        this.dataSource = dataSource;
    }

    /**
     * Verifica a saúde do banco de dados
     */
    @Override
    public Health health() {
        try {
            // Marca o horário de início pra medir o tempo de resposta
            Instant start = Instant.now();

            // Testa se a conexão tá válida
            boolean isValid = databaseConfig.isConnectionValid();

            Duration responseTime = Duration.between(start, Instant.now());

            if (isValid) {
                // Se tá válido, retorna UP com os detalhes
                return Health.up()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("connectionValid", true)
                    .withDetail("responseTime", responseTime.toMillis() + "ms")
                    .withDetail("connectionPool", getConnectionPoolStatus())
                    .build();
            } else {
                // Se não tá válido, retorna DOWN
                return Health.down()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("connectionValid", false)
                    .withDetail("responseTime", responseTime.toMillis() + "ms")
                    .withDetail("error", "Database connection test failed")
                    .build();
            }

        } catch (Exception e) {
            // Se deu algum erro, retorna DOWN com a mensagem de erro
            logger.error("Database health check failed", e);
            return Health.down()
                .withDetail("database", "PostgreSQL")
                .withDetail("connectionValid", false)
                .withDetail("error", e.getMessage())
                .build();
        }
    }

    /**
     * Verifica o status do connection pool
     */
    private String getConnectionPoolStatus() {
        try (Connection connection = dataSource.getConnection()) {
            // Se conseguiu pegar uma conexão, tá OK
            return "Active connection obtained";
        } catch (SQLException e) {
            logger.warn("Failed to obtain connection for pool status check: {}", e.getMessage());
            return "Failed to obtain connection";
        }
    }
}
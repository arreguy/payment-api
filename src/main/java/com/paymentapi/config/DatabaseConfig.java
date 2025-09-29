package com.paymentapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.function.Supplier;

@Component
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static final String CORRELATION_ID = "correlationId";
    private static final String TEST_QUERY = "SELECT 1";

    private final JdbcTemplate jdbcTemplate;

    public DatabaseConfig(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void validateDatabaseConnection() {
        executeWithCorrelationId(() -> {
            logger.info("Starting database connection validation");
            testConnection();
            logger.info("Database connection validation successful");
            return null;
        }, "Failed to establish database connection");
    }

    public void testConnection() throws DataAccessException {
        try {
            logger.debug("Executing database connectivity test query");
            Integer result = jdbcTemplate.queryForObject(TEST_QUERY, Integer.class);

            if (!Integer.valueOf(1).equals(result)) {
                throw new DataAccessResourceFailureException("Unexpected result from database test query: " + result);
            }

            logger.debug("Database test query executed successfully");
        } catch (DataAccessException e) {
            logger.warn("Database connection test failed: {}", e.getMessage());
            throw e;
        }
    }

    public boolean isConnectionValid() {
        return executeWithCorrelationId(() -> {
            try {
                testConnection();
                return true;
            } catch (Exception e) {
                logger.warn("Database connection validation failed: {}", e.getMessage());
                return false;
            }
        }, null);
    }

    private <T> T executeWithCorrelationId(Supplier<T> supplier, String errorMessage) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put(CORRELATION_ID, correlationId);

        try {
            return supplier.get();
        } catch (Exception e) {
            if (errorMessage != null) {
                logger.error("{}: {}", errorMessage, e.getMessage(), e);
                throw new RuntimeException(errorMessage, e);
            }
            throw e;
        } finally {
            MDC.remove(CORRELATION_ID);
        }
    }
}
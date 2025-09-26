package com.paymentapi.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseConfigTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private DatabaseConfig databaseConfig;

    @BeforeEach
    void setUp() {
        databaseConfig = new DatabaseConfig(jdbcTemplate);
    }

    @Test
    void testConnection_Success() throws DataAccessException {
        // Given
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class))
            .thenReturn(1);

        // When & Then
        assertDoesNotThrow(() -> databaseConfig.testConnection());
        verify(jdbcTemplate).queryForObject("SELECT 1", Integer.class);
    }

    @Test
    void testConnection_Failure() throws DataAccessException {
        // Given
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class))
            .thenThrow(new DataAccessException("Connection failed") {});

        // When & Then
        assertThrows(DataAccessException.class, () -> databaseConfig.testConnection());
        verify(jdbcTemplate).queryForObject("SELECT 1", Integer.class);
    }

    @Test
    void testConnection_UnexpectedResult() throws DataAccessException {
        // Given
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class))
            .thenReturn(null);

        // When & Then
        assertThrows(DataAccessException.class, () -> databaseConfig.testConnection());
        verify(jdbcTemplate).queryForObject("SELECT 1", Integer.class);
    }

    @Test
    void testConnection_UnexpectedValue() throws DataAccessException {
        // Given
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class))
            .thenReturn(2);

        // When & Then
        assertThrows(DataAccessException.class, () -> databaseConfig.testConnection());
        verify(jdbcTemplate).queryForObject("SELECT 1", Integer.class);
    }

    @Test
    void isConnectionValid_Success() {
        // Given
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class))
            .thenReturn(1);

        // When
        boolean result = databaseConfig.isConnectionValid();

        // Then
        assertTrue(result);
        verify(jdbcTemplate).queryForObject("SELECT 1", Integer.class);
    }

    @Test
    void isConnectionValid_Failure() {
        // Given
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class))
            .thenThrow(new DataAccessException("Connection failed") {});

        // When
        boolean result = databaseConfig.isConnectionValid();

        // Then
        assertFalse(result);
        verify(jdbcTemplate).queryForObject("SELECT 1", Integer.class);
    }
}
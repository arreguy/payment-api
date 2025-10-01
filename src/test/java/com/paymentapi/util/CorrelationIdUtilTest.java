package com.paymentapi.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

/**
 * Testes unitários pra classe CorrelationIdUtil.
 */
class CorrelationIdUtilTest {

    @BeforeEach
    void setUp() {
        // Clear MDC before each test
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        // Clear MDC after each test
        MDC.clear();
    }

    @Test
    void testGenerateCorrelationId_shouldGenerateValidUuid() {
        // Arrange & Act
        String correlationId = CorrelationIdUtil.generateCorrelationId();

        // Assert
        assertNotNull(correlationId);
        assertTrue(correlationId.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    @Test
    void testGenerateCorrelationId_shouldGenerateUniqueIds() {
        // Arrange & Act
        String correlationId1 = CorrelationIdUtil.generateCorrelationId();
        String correlationId2 = CorrelationIdUtil.generateCorrelationId();

        // Assert
        assertNotNull(correlationId1);
        assertNotNull(correlationId2);
        assertNotEquals(correlationId1, correlationId2);
    }

    @Test
    void testSetAndGetCorrelationId_shouldStoreInMdc() {
        // Arrange
        String testCorrelationId = "test-correlation-id-123";

        // Act
        CorrelationIdUtil.setCorrelationId(testCorrelationId);
        String retrieved = CorrelationIdUtil.getCorrelationId();

        // Assert
        assertEquals(testCorrelationId, retrieved);
    }

    @Test
    void testSetCorrelationId_withNullValue_shouldNotSetInMdc() {
        // Arrange & Act
        CorrelationIdUtil.setCorrelationId(null);
        String retrieved = CorrelationIdUtil.getCorrelationId();

        // Assert
        assertNull(retrieved);
    }

    @Test
    void testSetCorrelationId_withBlankValue_shouldNotSetInMdc() {
        // Arrange & Act
        CorrelationIdUtil.setCorrelationId("   ");
        String retrieved = CorrelationIdUtil.getCorrelationId();

        // Assert
        assertNull(retrieved);
    }

    @Test
    void testClearCorrelationId_shouldRemoveFromMdc() {
        // Arrange
        String testCorrelationId = "test-correlation-id-456";
        CorrelationIdUtil.setCorrelationId(testCorrelationId);

        // Act
        CorrelationIdUtil.clearCorrelationId();
        String retrieved = CorrelationIdUtil.getCorrelationId();

        // Assert
        assertNull(retrieved);
    }

    @Test
    void testClearAllMdc_shouldRemoveAllContexts() {
        // Arrange
        CorrelationIdUtil.setCorrelationId("test-correlation-id");
        CorrelationIdUtil.setMdcContext("userId", "user123");
        CorrelationIdUtil.setMdcContext("requestPath", "/api/test");

        // Act
        CorrelationIdUtil.clearAllMdc();

        // Assert
        assertNull(CorrelationIdUtil.getCorrelationId());
        assertNull(MDC.get("userId"));
        assertNull(MDC.get("requestPath"));
    }

    @Test
    void testGetCorrelationIdHeader_shouldReturnStandardHeader() {
        // Arrange & Act
        String header = CorrelationIdUtil.getCorrelationIdHeader();

        // Assert
        assertEquals("X-Correlation-ID", header);
    }

    @Test
    void testSetMdcContext_shouldStoreCustomContext() {
        // Arrange
        String key = "customKey";
        String value = "customValue";

        // Act
        CorrelationIdUtil.setMdcContext(key, value);
        String retrieved = MDC.get(key);

        // Assert
        assertEquals(value, retrieved);
    }

    @Test
    void testSetMdcContext_withNullKey_shouldNotThrowException() {
        // Arrange & Act & Assert
        CorrelationIdUtil.setMdcContext(null, "value");
        // Should not throw exception
    }

    @Test
    void testSetMdcContext_withNullValue_shouldNotSetInMdc() {
        // Arrange & Act
        CorrelationIdUtil.setMdcContext("testKey", null);
        String retrieved = MDC.get("testKey");

        // Assert
        assertNull(retrieved);
    }

    @Test
    void testRemoveMdcContext_shouldRemoveSpecificKey() {
        // Arrange
        CorrelationIdUtil.setMdcContext("key1", "value1");
        CorrelationIdUtil.setMdcContext("key2", "value2");

        // Act
        CorrelationIdUtil.removeMdcContext("key1");

        // Assert
        assertNull(MDC.get("key1"));
        assertEquals("value2", MDC.get("key2"));
    }

    @Test
    void testRemoveMdcContext_withNullKey_shouldNotThrowException() {
        // Arrange & Act & Assert
        CorrelationIdUtil.removeMdcContext(null);
    }
}

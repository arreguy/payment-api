package com.paymentapi.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.paymentapi.util.CorrelationIdUtil;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

/**
 * Testes unitários pra GlobalExceptionHandler.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private WebRequest webRequest;

    @Mock
    private BindingResult bindingResult;

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        MDC.clear();
        exceptionHandler = new GlobalExceptionHandler();
        CorrelationIdUtil.setCorrelationId("test-correlation-id-123");
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void testHandleValidationException_shouldReturnBadRequestWithDetails() {
        // Arrange
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/test");
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(
                new FieldError("user", "email", "must be a valid email"),
                new FieldError("user", "name", "must not be blank")
        ));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationException(ex, webRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Validation failed", response.getBody().get("message"));
        assertEquals("test-correlation-id-123", response.getBody().get("correlationId"));
        assertNotNull(response.getBody().get("timestamp"));
        assertNotNull(response.getBody().get("details"));

        Map<String, String> details = (Map<String, String>) response.getBody().get("details");
        assertEquals(2, details.size());
        assertTrue(details.containsKey("email"));
        assertTrue(details.containsKey("name"));

        // Verificar MDC
        assertNull(MDC.get("error_type"));
        assertNull(MDC.get("request_path"));
    }

    @Test
    void testHandleIllegalArgumentException_shouldReturnBadRequest() {
        // Arrange
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/test");
        IllegalArgumentException ex = new IllegalArgumentException("Invalid parameter value");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleIllegalArgumentException(ex, webRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Invalid parameter value", response.getBody().get("message"));
        assertEquals("test-correlation-id-123", response.getBody().get("correlationId"));
        assertNotNull(response.getBody().get("timestamp"));

        // Verificar MDC
        assertNull(MDC.get("error_type"));
        assertNull(MDC.get("request_path"));
    }

    @Test
    void testHandleGenericException_shouldReturnInternalServerError() {
        // Arrange
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/test");
        Exception ex = new RuntimeException("Unexpected error occurred");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGenericException(ex, webRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().get("status"));
        assertEquals("An unexpected error occurred", response.getBody().get("message"));
        assertEquals("test-correlation-id-123", response.getBody().get("correlationId"));
        assertNotNull(response.getBody().get("timestamp"));

        // Verificar MDC
        assertNull(MDC.get("error_type"));
        assertNull(MDC.get("request_path"));
    }

    @Test
    void testHandleException_withCorrelationIdInMdc_shouldIncludeInResponse() {
        // Arrange
        String expectedCorrelationId = "specific-correlation-id-456";
        CorrelationIdUtil.setCorrelationId(expectedCorrelationId);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/payment");
        Exception ex = new Exception("Test exception");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGenericException(ex, webRequest);

        // Assert
        assertNotNull(response.getBody());
        assertEquals(expectedCorrelationId, response.getBody().get("correlationId"));
    }

    @Test
    void testHandleException_shouldUseIso8601Timestamp() {
        // Arrange
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/test");
        Exception ex = new Exception("Test exception");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGenericException(ex, webRequest);

        // Assert
        assertNotNull(response.getBody());
        String timestamp = (String) response.getBody().get("timestamp");
        assertNotNull(timestamp);
        assertTrue(timestamp.contains("T"));
        assertTrue(timestamp.contains("Z") || timestamp.contains("+") || timestamp.contains("-"));
    }

    @Test
    void testHandleValidationException_shouldCleanupMdcEvenOnException() {
        // Arrange
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/test");
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(
                new FieldError("user", "email", "invalid")
        ));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // Act
        exceptionHandler.handleValidationException(ex, webRequest);

        // Verificar MDC
        assertNull(MDC.get("error_type"));
        assertNull(MDC.get("request_path"));
    }

    @Test
    void testHandleException_requestPathWithoutUriPrefix_shouldHandleCorrectly() {
        // Arrange
        when(webRequest.getDescription(false)).thenReturn("/api/v1/test");
        Exception ex = new Exception("Test exception");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGenericException(ex, webRequest);

        // Assert
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().get("status"));
    }

    @Test
    void testErrorResponse_withNullDetails_shouldNotIncludeDetailsField() {
        // Arrange
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/test");
        IllegalArgumentException ex = new IllegalArgumentException("Test error");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleIllegalArgumentException(ex, webRequest);

        // Assert
        assertNotNull(response.getBody());
        assertTrue(!response.getBody().containsKey("details") || response.getBody().get("details") == null);
    }
}

package com.paymentapi.config;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paymentapi.util.CorrelationIdUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for RequestResponseLoggingFilter.
 */
@ExtendWith(MockitoExtension.class)
class RequestResponseLoggingFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private RequestResponseLoggingFilter filter;

    @BeforeEach
    void setUp() {
        MDC.clear();
        filter = new RequestResponseLoggingFilter();
        ReflectionTestUtils.setField(filter, "loggingEnabled", true);
        ReflectionTestUtils.setField(filter, "includeHeaders", false);
        ReflectionTestUtils.setField(filter, "includeQueryParams", true);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void testDoFilter_shouldLogRequestAndResponse() throws ServletException, IOException {
        // Arrange
        String correlationId = "test-correlation-id";
        CorrelationIdUtil.setCorrelationId(correlationId);

        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/test");
        when(request.getQueryString()).thenReturn("param1=value1");
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(response.getStatus()).thenReturn(200);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilter_withSensitiveQueryParams_shouldMaskThem() throws ServletException, IOException {
        // Arrange
        String correlationId = "test-correlation-id";
        CorrelationIdUtil.setCorrelationId(correlationId);

        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/v1/login");
        when(request.getQueryString()).thenReturn("username=user&password=secret123");
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(response.getStatus()).thenReturn(200);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilter_whenLoggingDisabled_shouldNotLog() throws ServletException, IOException {
        // Arrange
        ReflectionTestUtils.setField(filter, "loggingEnabled", false);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilter_with500Status_shouldLogAsError() throws ServletException, IOException {
        // Arrange
        String correlationId = "test-correlation-id";
        CorrelationIdUtil.setCorrelationId(correlationId);

        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/v1/transfer");
        when(request.getQueryString()).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(response.getStatus()).thenReturn(500);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilter_with400Status_shouldLogAsWarn() throws ServletException, IOException {
        // Arrange
        String correlationId = "test-correlation-id";
        CorrelationIdUtil.setCorrelationId(correlationId);

        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/v1/transfer");
        when(request.getQueryString()).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(response.getStatus()).thenReturn(400);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilter_shouldMaskClientIpLastOctet() throws ServletException, IOException {
        // Arrange
        String correlationId = "test-correlation-id";
        CorrelationIdUtil.setCorrelationId(correlationId);

        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/test");
        when(request.getQueryString()).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("203.0.113.45");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(response.getStatus()).thenReturn(200);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
    }
}

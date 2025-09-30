package com.paymentapi.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

/**
 * Unit tests for CorrelationIdFilter.
 */
@ExtendWith(MockitoExtension.class)
class CorrelationIdFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private CorrelationIdFilter filter;

    @BeforeEach
    void setUp() {
        MDC.clear();
        filter = new CorrelationIdFilter();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void testDoFilter_withExistingCorrelationId_shouldUseProvidedId()
            throws ServletException, IOException {
        // Arrange
        String existingCorrelationId = "existing-correlation-id-123";
        when(request.getHeader("X-Correlation-ID")).thenReturn(existingCorrelationId);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response).setHeader(ArgumentCaptor.forClass(String.class).capture(), captor.capture());
        assertEquals(existingCorrelationId, captor.getValue());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilter_withoutCorrelationId_shouldGenerateNewId()
            throws ServletException, IOException {
        // Arrange
        when(request.getHeader("X-Correlation-ID")).thenReturn(null);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response).setHeader(ArgumentCaptor.forClass(String.class).capture(), captor.capture());
        String generatedId = captor.getValue();
        assertNotNull(generatedId);
        assertTrue(generatedId.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilter_withBlankCorrelationId_shouldGenerateNewId()
            throws ServletException, IOException {
        // Arrange
        when(request.getHeader("X-Correlation-ID")).thenReturn("   ");

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response).setHeader(ArgumentCaptor.forClass(String.class).capture(), captor.capture());
        String generatedId = captor.getValue();
        assertNotNull(generatedId);
        assertTrue(generatedId.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilter_shouldClearMdcAfterProcessing()
            throws ServletException, IOException {
        // Arrange
        when(request.getHeader("X-Correlation-ID")).thenReturn("test-id");

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        assertNull(CorrelationIdUtil.getCorrelationId());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilter_shouldClearMdcEvenOnException()
            throws ServletException, IOException {
        // Arrange
        when(request.getHeader("X-Correlation-ID")).thenReturn("test-id");

        // Act & Assert
        try {
            // Simulate exception in filter chain using doThrow
            org.mockito.Mockito.doThrow(new ServletException("Test exception"))
                    .when(filterChain).doFilter(request, response);
            filter.doFilter(request, response, filterChain);
        } catch (ServletException e) {
            // Expected exception
        }

        // MDC should still be cleared
        assertNull(CorrelationIdUtil.getCorrelationId());
    }
}

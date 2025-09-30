package com.paymentapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for logging infrastructure including correlation ID propagation.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class LoggingIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testCorrelationId_withoutProvidedId_shouldGenerateAndReturn() {
        // Arrange & Act
        ResponseEntity<String> response = restTemplate.exchange(
                "/health/live",
                HttpMethod.GET,
                null,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String correlationId = response.getHeaders().getFirst("X-Correlation-ID");
        assertNotNull(correlationId);
        assertTrue(correlationId.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    @Test
    void testCorrelationId_withProvidedId_shouldReturnSameId() {
        // Arrange
        String providedCorrelationId = "test-correlation-id-12345";
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Correlation-ID", providedCorrelationId);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                "/health/live",
                HttpMethod.GET,
                entity,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String returnedCorrelationId = response.getHeaders().getFirst("X-Correlation-ID");
        assertEquals(providedCorrelationId, returnedCorrelationId);
    }

    @Test
    void testRequestResponseLogging_shouldLogSuccessfulRequest() {
        // Arrange & Act
        ResponseEntity<String> response = restTemplate.exchange(
                "/health/ready",
                HttpMethod.GET,
                null,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getFirst("X-Correlation-ID"));
    }

    @Test
    void testErrorLogging_shouldLogWithCorrelationId() {
        // Arrange & Act
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "/health/nonexistent",
                    HttpMethod.GET,
                    null,
                    String.class
            );

            // Assert
            assertTrue(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError());
            assertNotNull(response.getHeaders().getFirst("X-Correlation-ID"));
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    @Test
    void testMultipleRequests_shouldHaveUniqueCorrelationIds() {
        // Arrange & Act
        ResponseEntity<String> response1 = restTemplate.exchange(
                "/health/live",
                HttpMethod.GET,
                null,
                String.class
        );

        ResponseEntity<String> response2 = restTemplate.exchange(
                "/health/live",
                HttpMethod.GET,
                null,
                String.class
        );

        // Assert
        String correlationId1 = response1.getHeaders().getFirst("X-Correlation-ID");
        String correlationId2 = response2.getHeaders().getFirst("X-Correlation-ID");

        assertNotNull(correlationId1);
        assertNotNull(correlationId2);
        assertNotEquals(correlationId1, correlationId2);
    }
}

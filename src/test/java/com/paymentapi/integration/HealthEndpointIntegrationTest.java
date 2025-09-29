package com.paymentapi.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class HealthEndpointIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testHealthReadyEndpoint_ShouldReturnHealthStatus() {
        // Act
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/v1/health/ready", Map.class);

        // Assert
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("status");

        String status = (String) response.getBody().get("status");
        assertThat(status).isIn("UP", "DOWN");

        // Check components structure if present
        if (response.getBody().containsKey("components")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> components = (Map<String, Object>) response.getBody().get("components");
            assertThat(components).isNotNull();
        }
    }

    @Test
    void testHealthLiveEndpoint_ShouldAlwaysReturnUp() {
        // Act
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/v1/health/live", Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry("status", "UP");

        // Check application component
        @SuppressWarnings("unchecked")
        Map<String, Object> components = (Map<String, Object>) response.getBody().get("components");
        assertThat(components).containsKey("application");

        @SuppressWarnings("unchecked")
        Map<String, Object> application = (Map<String, Object>) components.get("application");
        assertThat(application).containsEntry("status", "UP");
    }

    @Test
    void testHealthStartupEndpoint_ShouldReturnStartupStatus() {
        // Act
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/v1/health/startup", Map.class);

        // Assert
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("status");

        String status = (String) response.getBody().get("status");
        assertThat(status).isIn("UP", "DOWN");
    }

    @Test
    void testHealthEndpoints_ShouldReturnJsonContentType() {
        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/v1/health/live", String.class);

        // Assert
        assertThat(response.getHeaders().getContentType().toString())
            .contains("application/json");
    }

    @Test
    void testHealthEndpoints_ShouldBeAccessibleWithoutAuthentication() {
        // This test verifies that health endpoints don't require authentication
        // by successfully calling them without providing credentials

        // Act & Assert - Ready endpoint
        ResponseEntity<Map> readyResponse = restTemplate.getForEntity(
            "http://localhost:" + port + "/v1/health/ready", Map.class);
        assertThat(readyResponse.getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);

        // Act & Assert - Live endpoint
        ResponseEntity<Map> liveResponse = restTemplate.getForEntity(
            "http://localhost:" + port + "/v1/health/live", Map.class);
        assertThat(liveResponse.getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);

        // Act & Assert - Startup endpoint
        ResponseEntity<Map> startupResponse = restTemplate.getForEntity(
            "http://localhost:" + port + "/v1/health/startup", Map.class);
        assertThat(startupResponse.getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
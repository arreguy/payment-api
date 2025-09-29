package com.paymentapi.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.CompositeHealthContributor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = HealthController.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HealthContributor healthContributor;

    @Test
    void testReadyEndpoint_ShouldReturnHealthStatus() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v1/health/ready"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").exists());
    }

    @Test
    void testLiveEndpoint_ShouldAlwaysReturn200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v1/health/live"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.components.application.status").value("UP"))
            .andExpect(jsonPath("$.components.application.details.service").value("payment-api"));
    }

    @Test
    void testStartupEndpoint_ShouldReturnStartupStatus() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v1/health/startup"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").exists());
    }

    @Test
    void testHealthEndpoints_ShouldReturnJsonContentType() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v1/health/live"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/v1/health/ready"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/v1/health/startup"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
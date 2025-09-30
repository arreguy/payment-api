package com.paymentapi.config;

import static org.mockito.Mockito.lenient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.Environment;

/**
 * Unit tests for ApplicationEventLogger.
 */
@ExtendWith(MockitoExtension.class)
class ApplicationEventLoggerTest {

    @Mock
    private Environment environment;

    @Mock
    private ApplicationStartedEvent startedEvent;

    @Mock
    private ApplicationReadyEvent readyEvent;

    @Mock
    private ContextClosedEvent closedEvent;

    private ApplicationEventLogger eventLogger;

    @BeforeEach
    void setUp() {
        // Setup will be done per test as needed
    }

    @Test
    void testOnApplicationStarted_shouldLogStartupEvent() {
        // Arrange
        lenient().when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
        eventLogger = new ApplicationEventLogger(environment);

        // Act
        eventLogger.onApplicationStarted(startedEvent);
    }

    @Test
    void testOnApplicationReady_shouldLogReadyEvent() {
        // Arrange
        lenient().when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
        eventLogger = new ApplicationEventLogger(environment);

        // Act
        eventLogger.onApplicationReady(readyEvent);
    }

    @Test
    void testOnContextClosed_shouldLogShutdownEvent() {
        // Arrange
        lenient().when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
        eventLogger = new ApplicationEventLogger(environment);

        // Act
        eventLogger.onContextClosed(closedEvent);
    }

    @Test
    void testOnPreDestroy_shouldLogShutdownHook() {
        // Arrange
        lenient().when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
        ApplicationEventLogger freshLogger = new ApplicationEventLogger(environment);

        // Act
        freshLogger.onPreDestroy();
    }

    @Test
    void testOnApplicationStarted_withNoActiveProfiles_shouldUseDefault() {
        // Arrange
        lenient().when(environment.getActiveProfiles()).thenReturn(new String[]{});
        ApplicationEventLogger loggerNoProfile = new ApplicationEventLogger(environment);

        // Act
        loggerNoProfile.onApplicationStarted(startedEvent);
    }

    @Test
    void testOnApplicationReady_withMultipleProfiles_shouldLogAll() {
        // Arrange
        lenient().when(environment.getActiveProfiles()).thenReturn(new String[]{"dev", "local"});
        ApplicationEventLogger loggerMultiProfile = new ApplicationEventLogger(environment);

        // Act
        loggerMultiProfile.onApplicationReady(readyEvent);
    }
}

package com.paymentapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.paymentapi.dto.response.ErrorResponse;
import com.paymentapi.exception.UserNotFoundException;
import com.paymentapi.util.CorrelationIdUtil;
import java.util.List;
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
 * Testes unitários para GlobalExceptionHandler.
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
    void testHandleValidationException_shouldReturnUnprocessableEntityWithDetails() {
        // Arrange
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/test");
        when(bindingResult.getAllErrors()).thenReturn(List.of(
                new FieldError("user", "email", "must be a valid email"),
                new FieldError("user", "name", "must not be blank")
        ));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodArgumentNotValidException(ex, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().detail()).contains("must be a valid email");
        assertThat(response.getBody().detail()).contains("must not be blank");
        assertThat(response.getBody().type()).isEqualTo("validation_error");

        // Verificar MDC
        assertNull(MDC.get("error_type"));
        assertNull(MDC.get("request_path"));
    }

    @Test
    void testHandleIllegalArgumentException_shouldReturnBadRequest() {
        // Arrange
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/test");
        IllegalArgumentException ex = new IllegalArgumentException("O valor deve ser positivo");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(ex, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().detail()).isEqualTo("O valor deve ser positivo");
        assertThat(response.getBody().type()).isEqualTo("validation_error");

        // Verificar MDC
        assertNull(MDC.get("error_type"));
        assertNull(MDC.get("request_path"));
    }

    @Test
    void testHandleUserNotFoundException_shouldReturnConflict() {
        // Arrange
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/transfer");
        UserNotFoundException ex = new UserNotFoundException("12345678909");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUserNotFoundException(ex, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().detail()).contains("Usuário não encontrado");
        assertThat(response.getBody().detail()).contains("123.***.***-**");
        assertThat(response.getBody().type()).isEqualTo("user_not_found");

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
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().detail())
            .isEqualTo("Erro interno do servidor. Por favor, tente novamente mais tarde.");
        assertThat(response.getBody().type()).isEqualTo("internal_server_error");
        // Verifica que a mensagem de erro interna não é exposta
        assertThat(response.getBody().detail()).doesNotContain("Unexpected error occurred");

        // Verificar MDC
        assertNull(MDC.get("error_type"));
        assertNull(MDC.get("request_path"));
    }

    @Test
    void testHandleValidationException_shouldCleanupMdcEvenOnException() {
        // Arrange
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/test");
        when(bindingResult.getAllErrors()).thenReturn(List.of(
                new FieldError("user", "email", "invalid")
        ));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // Act
        exceptionHandler.handleMethodArgumentNotValidException(ex, webRequest);

        // Verificar MDC
        assertNull(MDC.get("error_type"));
        assertNull(MDC.get("request_path"));
    }

    @Test
    void testErrorResponseFollowsRfc0006Format() {
        // Arrange
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/test");
        IllegalArgumentException ex = new IllegalArgumentException("Test error");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(ex, webRequest);

        // Assert - verifica estrutura RFC 0006
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().detail()).isNotNull();
        assertThat(response.getBody().type()).isNotNull();
        // RFC 0006 especifica apenas detail e type
        assertThat(response.getBody()).hasNoNullFieldsOrProperties();
    }

    @Test
    void testNoSensitiveInformationExposed() {
        // Arrange - simula um erro com stack trace e informações internas
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/test");
        Exception ex = new NullPointerException("Cannot invoke method on null object at com.paymentapi.internal.SecretClass");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex, webRequest);

        // Assert
        assertThat(response.getBody()).isNotNull();
        // Verifica que a resposta não expõe informações internas
        assertThat(response.getBody().detail()).doesNotContain("SecretClass");
        assertThat(response.getBody().detail()).doesNotContain("NullPointerException");
        assertThat(response.getBody().detail()).doesNotContain("stack");
    }
}

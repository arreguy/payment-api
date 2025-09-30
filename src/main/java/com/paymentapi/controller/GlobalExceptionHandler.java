package com.paymentapi.controller;

import com.paymentapi.util.CorrelationIdUtil;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Handler global de exceções que captura erros de todos os controllers
 * e loga eles com contexto estruturado incluindo correlation IDs, stack traces e informações da requisição.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Trata os erros de validação das anotações @Valid
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        // Pega as informações do contexto
        String correlationId = CorrelationIdUtil.getCorrelationId();
        String requestPath = getRequestPath(request);
        String errorType = ex.getClass().getSimpleName();

        try {
            // Coloca informações de erro no MDC
            CorrelationIdUtil.setMdcContext("error_type", errorType);
            CorrelationIdUtil.setMdcContext("request_path", requestPath);

            // Monta o mapa com os erros de validação
            Map<String, String> validationErrors = new HashMap<>();
            ex.getBindingResult().getFieldErrors().forEach(error ->
                    validationErrors.put(error.getField(), error.getDefaultMessage())
            );

            // Loga o erro
            logger.warn(
                    "Validation error: correlationId={} requestPath={} errorType={} validationErrors={}",
                    correlationId,
                    requestPath,
                    errorType,
                    validationErrors
            );

            // Monta a resposta de erro
            Map<String, Object> errorResponse = buildErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    "Validation failed",
                    correlationId,
                    validationErrors
            );

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } finally {
            // Limpa o contexto de erro do MDC
            cleanupErrorContext();
        }
    }

    /**
     * Trata exceções de argumento ilegal
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request) {

        String correlationId = CorrelationIdUtil.getCorrelationId();
        String requestPath = getRequestPath(request);
        String errorType = ex.getClass().getSimpleName();

        try {
            // Coloca informações de erro no MDC
            CorrelationIdUtil.setMdcContext("error_type", errorType);
            CorrelationIdUtil.setMdcContext("request_path", requestPath);

            // Loga o erro
            logger.warn(
                    "Illegal argument error: correlationId={} requestPath={} errorType={} message={}",
                    correlationId,
                    requestPath,
                    errorType,
                    ex.getMessage()
            );

            // Monta a resposta de erro
            Map<String, Object> errorResponse = buildErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    ex.getMessage(),
                    correlationId,
                    null
            );

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } finally {
            // Limpa o contexto de erro do MDC
            cleanupErrorContext();
        }
    }

    /**
     * Trata todas as outras exceções não capturadas, logando com stack trace completo
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex,
            WebRequest request) {

        String correlationId = CorrelationIdUtil.getCorrelationId();
        String requestPath = getRequestPath(request);
        String errorType = ex.getClass().getSimpleName();

        try {
            // Coloca informações de erro no MDC
            CorrelationIdUtil.setMdcContext("error_type", errorType);
            CorrelationIdUtil.setMdcContext("request_path", requestPath);

            // Loga com nível ERROR - o Logback inclui o stack trace automaticamente
            logger.error(
                    "Unexpected error: correlationId={} requestPath={} errorType={} message={}",
                    correlationId,
                    requestPath,
                    errorType,
                    ex.getMessage(),
                    ex // Passa a exceção pro Logback incluir o stack trace nos logs
            );

            // Monta a resposta de erro genérica
            Map<String, Object> errorResponse = buildErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred",
                    correlationId,
                    null
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } finally {
            // Limpa o contexto de erro do MDC
            cleanupErrorContext();
        }
    }

    /**
     * Monta a resposta de erro padronizada
     */
    private Map<String, Object> buildErrorResponse(
            HttpStatus status,
            String message,
            String correlationId,
            Object details) {

        // Cria o mapa com as informações do erro
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("correlationId", correlationId);
        errorResponse.put("timestamp", Instant.now().toString());

        // Adiciona detalhes se tiver
        if (details != null) {
            errorResponse.put("details", details);
        }

        return errorResponse;
    }

    /**
     * Extrai o path da requisição do WebRequest
     */
    private String getRequestPath(WebRequest request) {
        String path = request.getDescription(false);
        // Remove o prefixo "uri=" se tiver
        return path.startsWith("uri=") ? path.substring(4) : path;
    }

    /**
     * Limpa as chaves de contexto de erro do MDC
     */
    private void cleanupErrorContext() {
        CorrelationIdUtil.removeMdcContext("error_type");
        CorrelationIdUtil.removeMdcContext("request_path");
    }
}

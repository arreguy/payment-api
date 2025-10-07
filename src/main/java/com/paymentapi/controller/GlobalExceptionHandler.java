package com.paymentapi.controller;

import com.paymentapi.dto.response.ErrorResponse;
import com.paymentapi.exception.UserNotFoundException;
import com.paymentapi.util.CorrelationIdUtil;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Manipulador global de exceções para toda a aplicação.
 * Este componente intercepta exceções lançadas pelos controllers e as transforma
 * em respostas HTTP padronizadas
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Trata erros de validação de Bean Validation (annotations @NotNull, @Positive, etc).
     * <p>
     * Retorna HTTP 422 Unprocessable Entity com detalhes das violações de validação
     * no formato RFC 0006.
     *
     * @param ex exceção de validação lançada pelo Spring
     * @param request contexto da requisição web
     * @return ResponseEntity com ErrorResponse e HTTP 422
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
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

            // Coleta todas as mensagens de erro de validação
            String detail = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining("; "));

            // Loga o erro
            logger.warn(
                    "Validation error: correlationId={} requestPath={} errorType={} detail={}",
                    correlationId,
                    requestPath,
                    errorType,
                    detail
            );

            // Monta a resposta de erro
            ErrorResponse errorResponse = new ErrorResponse(
                detail,
                "validation_error"
            );

            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
        } finally {
            // Limpa o contexto de erro do MDC
            cleanupErrorContext();
        }
    }

    /**
     * Trata exceções de usuário não encontrado durante validações.
     * <p>
     * Retorna HTTP 409 Conflict para indicar que a operação não pode prosseguir
     * porque o usuário referenciado não existe.
     *
     * @param ex exceção de usuário não encontrado
     * @param request contexto da requisição web
     * @return ResponseEntity com ErrorResponse e HTTP 409
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(
            UserNotFoundException ex,
            WebRequest request) {

        String correlationId = CorrelationIdUtil.getCorrelationId();
        String requestPath = getRequestPath(request);
        String errorType = ex.getClass().getSimpleName();

        try {
            CorrelationIdUtil.setMdcContext("error_type", errorType);
            CorrelationIdUtil.setMdcContext("request_path", requestPath);

            logger.warn(
                    "User not found: correlationId={} requestPath={} message={}",
                    correlationId,
                    requestPath,
                    ex.getMessage()
            );

            ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                "user_not_found"
            );

            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } finally {
            cleanupErrorContext();
        }
    }

    /**
     * Trata exceções de argumentos inválidos (principalmente valores monetários).
     * <p>
     * Retorna HTTP 400 Bad Request para indicar que a requisição contém dados inválidos.
     *
     * @param ex exceção de argumento ilegal
     * @param request contexto da requisição web
     * @return ResponseEntity com ErrorResponse e HTTP 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request) {

        String correlationId = CorrelationIdUtil.getCorrelationId();
        String requestPath = getRequestPath(request);
        String errorType = ex.getClass().getSimpleName();

        try {
            CorrelationIdUtil.setMdcContext("error_type", errorType);
            CorrelationIdUtil.setMdcContext("request_path", requestPath);

            logger.warn(
                    "Illegal argument error: correlationId={} requestPath={} errorType={} message={}",
                    correlationId,
                    requestPath,
                    errorType,
                    ex.getMessage()
            );

            ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                "validation_error"
            );

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } finally {
            cleanupErrorContext();
        }
    }

    /**
     * Trata exceções genéricas e inesperadas.
     * <p>
     * Retorna HTTP 500 Internal Server Error sem expor detalhes internos do erro.
     * O stack trace completo é logado para investigação posterior.
     *
     * @param ex exceção genérica
     * @param request contexto da requisição web
     * @return ResponseEntity com ErrorResponse e HTTP 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            WebRequest request) {

        String correlationId = CorrelationIdUtil.getCorrelationId();
        String requestPath = getRequestPath(request);
        String errorType = ex.getClass().getSimpleName();

        try {
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

            ErrorResponse errorResponse = new ErrorResponse(
                "Erro interno do servidor. Por favor, tente novamente mais tarde.",
                "internal_server_error"
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } finally {
            cleanupErrorContext();
        }
    }

    /**
     * Extrai o path da requisição do WebRequest
     */
    private String getRequestPath(WebRequest request) {
        String path = request.getDescription(false);
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

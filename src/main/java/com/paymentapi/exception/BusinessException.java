package com.paymentapi.exception;

/**
 * Exceção base para todas as exceções de lógica de negócio da aplicação.
 */
public class BusinessException extends RuntimeException {

    /**
     * Constrói uma BusinessException com mensagem de erro.
     *
     * @param message mensagem descritiva do erro
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * Constrói uma BusinessException com mensagem e causa raiz.
     *
     * @param message mensagem descritiva do erro
     * @param cause causa raiz da exceção
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}

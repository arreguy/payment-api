package com.paymentapi.exception;

/**
 * Exceção lançada quando senha fornecida não atende aos requisitos de complexidade.
 */
public class InvalidPasswordException extends BusinessException {

    /**
     * Constrói uma InvalidPasswordException com mensagem de erro.
     *
     * @param message mensagem descritiva informando qual regra de senha foi violada
     */
    public InvalidPasswordException(String message) {
        super(message);
    }

    /**
     * Constrói uma InvalidPasswordException com mensagem e causa raiz.
     *
     * @param message mensagem descritiva informando qual regra de senha foi violada
     * @param cause causa raiz da exceção
     */
    public InvalidPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}

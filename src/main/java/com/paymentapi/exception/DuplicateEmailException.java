package com.paymentapi.exception;

/**
 * Exceção lançada quando há tentativa de criar usuário com email já cadastrado no sistema.
 */
public class DuplicateEmailException extends BusinessException {

    /**
     * Constrói uma DuplicateEmailException com mensagem de erro.
     *
     * @param message mensagem descritiva informando o email duplicado
     */
    public DuplicateEmailException(String message) {
        super(message);
    }

    /**
     * Constrói uma DuplicateEmailException com mensagem e causa raiz.
     *
     * @param message mensagem descritiva informando o email duplicado
     * @param cause causa raiz da exceção
     */
    public DuplicateEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}

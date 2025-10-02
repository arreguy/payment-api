package com.paymentapi.exception;

/**
 * Exceção lançada quando há tentativa de criar usuário com CPF já cadastrado no sistema.
 */
public class DuplicateCpfException extends BusinessException {

    /**
     * Constrói uma DuplicateCpfException com mensagem de erro.
     *
     * @param message mensagem descritiva informando o CPF duplicado
     */
    public DuplicateCpfException(String message) {
        super(message);
    }

    /**
     * Constrói uma DuplicateCpfException com mensagem e causa raiz.
     *
     * @param message mensagem descritiva informando o CPF duplicado
     * @param cause causa raiz da exceção
     */
    public DuplicateCpfException(String message, Throwable cause) {
        super(message, cause);
    }
}

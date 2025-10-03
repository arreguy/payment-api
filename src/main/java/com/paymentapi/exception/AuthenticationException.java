package com.paymentapi.exception;

import com.paymentapi.util.SecurityUtil;

/**
 * Exceção lançada quando a autenticação de um usuário falha.
 */
public class AuthenticationException extends BusinessException {

    /**
     * Constrói uma AuthenticationException com username e motivo da falha.
     *
     * @param username nome de usuário (CPF ou email) que tentou autenticar
     * @param reason motivo da falha de autenticação (ex: "User not found", "Invalid credentials")
     */
    public AuthenticationException(String username, String reason) {
        super(String.format("Autenticação falhou para usuário %s: %s", SecurityUtil.maskCpf(username), reason));
    }
}

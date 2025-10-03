package com.paymentapi.exception;

import java.util.UUID;

/**
 * Exceção lançada quando um usuário não é encontrado pelo ID (não existe).
 */
public class UserNotFoundException extends BusinessException {

    /**
     * Constrói uma UserNotFoundException com o ID do usuário.
     *
     * @param userId o UUID do user que não foi encontrado
     */
    public UserNotFoundException(UUID userId) {
        super(String.format("Usuário não encontrado: %s", userId));
    }
}

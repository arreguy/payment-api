package com.paymentapi.exception;

import com.paymentapi.util.SecurityUtil;
import java.util.UUID;

/**
 * Exceção lançada quando um usuário não é encontrado no sistema.
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

    /**
     * Constrói uma UserNotFoundException com o CPF/CNPJ (mascarado) do usuário não encontrado.
     *
     * @param cpfOrCnpj CPF ou CNPJ do usuário não encontrado
     */
    public UserNotFoundException(String cpfOrCnpj) {
        super("Usuário não encontrado: " + SecurityUtil.maskCpf(cpfOrCnpj));
    }
}

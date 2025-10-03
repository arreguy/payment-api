package com.paymentapi.exception;

import java.util.UUID;

/**
 * Exceção lançada quando um usuário não tem saldo suficiente para uma operação de débito.
 */
public class InsufficientFundsException extends BusinessException {

    /**
     * Constrói uma InsufficientFundsException com os detalhes do usuário e do saldo.
     *
     * @param userId o UUID do usuário com saldo insuficiente
     * @param requestedAmount o valor requerido para a operação
     * @param currentBalance o saldo atual do usuário
     */
    public InsufficientFundsException(UUID userId, Integer requestedAmount, Integer currentBalance) {
        super(String.format(
            "Saldo insuficiente: usuário %s possui R$ %.2f, operação requer R$ %.2f",
            userId,
            currentBalance / 100.0,
            requestedAmount / 100.0
        ));
    }
}

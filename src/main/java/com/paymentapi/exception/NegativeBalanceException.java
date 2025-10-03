package com.paymentapi.exception;

import java.util.UUID;

/**
 * Exceção lançada quando o saldo de um usuário ficaria negativo.
 */
public class NegativeBalanceException extends BusinessException {

    /**
     * Constrói uma NegativeBalanceException com os detalhes do usuário e do saldo.
     *
     * @param userId o UUID do user qual saldo ficaria negativo
     * @param attemptedBalance tentativa de novo saldo que ficaria negativo
     */
    public NegativeBalanceException(UUID userId, Integer attemptedBalance) {
        super(String.format(
            "Saldo negativo não permitido para usuário comum: usuário %s, saldo resultante R$ %.2f",
            userId,
            attemptedBalance / 100.0
        ));
    }
}

package com.paymentapi.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de response para queries no saldo do user.
 *
 * @param userId UUID do user
 * @param walletBalance saldo do user
 * @param lastUpdated timestamp do último update no saldo
 */
public record WalletBalanceResponse(
    UUID userId,
    Integer walletBalance,
    LocalDateTime lastUpdated
) {
}

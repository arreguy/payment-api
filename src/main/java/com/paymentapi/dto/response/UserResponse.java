package com.paymentapi.dto.response;

import com.paymentapi.entity.enums.UserType;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para respostas contendo dados de usuário.
 */
public record UserResponse(
    UUID id,
    String nomeCompleto,
    String cpf,
    String email,
    UserType userType,
    Integer walletBalance,
    LocalDateTime createdAt
) {}

package com.paymentapi.dto.internal;

import com.paymentapi.entity.enums.UserType;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Contexto do usuário autenticado, contendo informações essenciais sem dados sensíveis.
 *
 * @param userId identificador único do usuário
 * @param nomeCompleto nome completo do usuário
 * @param email endereço de email do usuário
 * @param userType tipo do usuário (COMMON_USER ou MERCHANT)
 * @param authenticatedAt timestamp de quando a autenticação foi realizada
 */
public record UserContext(
    UUID userId,
    String nomeCompleto,
    String email,
    UserType userType,
    LocalDateTime authenticatedAt
) {
}

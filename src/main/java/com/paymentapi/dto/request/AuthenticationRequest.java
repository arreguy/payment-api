package com.paymentapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para requisições de autenticação de usuário.
 *
 * @param username CPF ou email do usuário
 * @param password senha em plain text
 */
public record AuthenticationRequest(
    @NotNull(message = "Username é obrigatório")
    @NotBlank(message = "Username não pode estar vazio")
    String username,

    @NotNull(message = "Password é obrigatório")
    @NotBlank(message = "Password não pode estar vazio")
    String password
) {
}

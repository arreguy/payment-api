package com.paymentapi.dto.request;

import com.paymentapi.entity.enums.UserType;
import com.paymentapi.validation.constraints.ValidCpf;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisições de criação de usuário.
 */
public record UserCreateRequest(
    @NotBlank(message = "Nome completo é obrigatório")
    String nomeCompleto,

    @NotBlank(message = "CPF é obrigatório")
    @ValidCpf
    String cpf,

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    String email,

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres")
    String senha,

    @NotNull(message = "Tipo de usuário é obrigatório")
    UserType userType
) {}

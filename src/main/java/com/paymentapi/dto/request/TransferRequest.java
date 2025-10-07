package com.paymentapi.dto.request;

import com.paymentapi.validation.constraints.ValidCpf;
import com.paymentapi.validation.constraints.ValidCpfOrCnpj;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * DTO para requisição de transferência entre usuários.
 *
 * @param value Valor da transferência em formato decimal (deve ser positivo)
 * @param payer CPF do pagador (11 dígitos sem formatação)
 * @param payee CPF (11 dígitos) ou CNPJ (14 dígitos) do recebedor sem formatação
 */
public record TransferRequest(
    @NotNull(message = "O valor da transferência é obrigatório")
    @Positive(message = "O valor da transferência deve ser positivo")
    BigDecimal value,

    @NotNull(message = "O CPF do pagador é obrigatório")
    @Pattern(regexp = "^[0-9]{11}$", message = "O CPF do pagador deve conter exatamente 11 dígitos")
    @ValidCpf
    String payer,

    @NotNull(message = "O CPF/CNPJ do recebedor é obrigatório")
    @Pattern(regexp = "^[0-9]{11,14}$", message = "O CPF/CNPJ do recebedor deve conter 11 ou 14 dígitos")
    @ValidCpfOrCnpj
    String payee
) {}

package com.paymentapi.dto.response;

/**
 * DTO para respostas de erro.
 * Formato da resposta:
 * <pre>
 * {
 *   "detail": "Descrição legível do erro para exibição ao usuário",
 *   "type": "tipo_do_erro_para_tratamento"
 * }
 * </pre>
 *
 * @param detail Descrição legível do erro para o usuário
 * @param type Tipo do erro para tratamento programático pelo cliente
 */
public record ErrorResponse(
    String detail,
    String type
) {}

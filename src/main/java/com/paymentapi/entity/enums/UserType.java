package com.paymentapi.entity.enums;

/**
 * Define o tipo dos usuários no sistema de pagamentos.
 * Esse enum determina as capacidades de transacionar e as regras de negócio para cada usuário.
 */
public enum UserType {
  /**
   * Usuário comum que pode tanto enviar quanto receber transferências.
   * Esse é o tipo padrão de usuário para consumidores individuais.
   */
  COMMON_USER,

  /**
   * Usuário do tipo lojista que pode apenas RECEBER transferências.
   * Lojistas não podem enviar transferências para outros usuários.
   * Essa restrição está na camada de service.
   */
  MERCHANT
}

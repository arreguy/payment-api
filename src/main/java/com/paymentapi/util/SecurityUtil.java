package com.paymentapi.util;

/**
 * Classe utilitária para operações relacionadas à segurança e proteção de dados.
 * Fornece métodos para mascaramento de dados sensíveis
 */
public final class SecurityUtil {

    private static final String CPF_PATTERN = "\\d{11}";
    private static final String CPF_MASK = ".***.***-**";

    private SecurityUtil() {
    }

    /**
     * Mascara CPF exibindo apenas os 3 primeiros dígitos.
     *
     * @param value valor a ser mascarado (CPF ou outro identificador)
     * @return valor mascarado se for CPF, ou valor original caso contrário
     */
    public static String maskCpf(String value) {
        if (value != null && value.matches(CPF_PATTERN)) {
            return value.substring(0, 3) + CPF_MASK;
        }
        return value;
    }
}

package com.paymentapi.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utilitário para conversão e validação de valores monetários.
 * <p>
 * Esta classe fornece métodos para converter entre representações decimais
 * (BigDecimal) e inteiras (Integer centavos) de valores monetários.
 * <p>
 * Regras críticas:
 * <ul>
 *   <li>Todos os valores monetários são armazenados internamente como Integer centavos</li>
 *   <li>Conversão usa arredondamento HALF_UP para precisão financeira</li>
 *   <li>Valores negativos ou excedendo Integer.MAX_VALUE são rejeitados</li>
 *   <li>Range válido: R$0.01 a R$21.474.836,47 (Integer.MAX_VALUE centavos)</li>
 * </ul>
 * <p>
 * Exemplos de conversão:
 * <ul>
 *   <li>R$100.50 -> 10050 centavos</li>
 *   <li>R$100.505 -> 10051 centavos (arredondamento HALF_UP)</li>
 *   <li>R$100.504 -> 10050 centavos (arredondamento HALF_UP)</li>
 *   <li>R$0.01 -> 1 centavo</li>
 * </ul>
 */
public final class MonetaryUtil {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal MAX_VALUE_IN_CENTS = new BigDecimal(Integer.MAX_VALUE);

    /**
     * Construtor privado para prevenir instanciação.
     * Esta é uma classe utilitária com apenas métodos estáticos.
     */
    private MonetaryUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Converte um valor decimal (BigDecimal) para Integer centavos.
     * <p>
     * O valor é multiplicado por 100 e arredondado usando RoundingMode.HALF_UP
     * para garantir precisão financeira adequada.
     *
     * @param value Valor decimal em reais (ex: 100.50 para R$100,50)
     * @return Valor em centavos como Integer (ex: 10050)
     * @throws IllegalArgumentException se value for null, negativo, zero, ou exceder Integer.MAX_VALUE
     */
    public static Integer convertToIntegerCents(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("O valor não pode ser nulo");
        }

        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor deve ser positivo");
        }

        BigDecimal cents = value.multiply(ONE_HUNDRED)
                .setScale(0, RoundingMode.HALF_UP);

        if (cents.compareTo(MAX_VALUE_IN_CENTS) > 0) {
            throw new IllegalArgumentException(
                "O valor excede o limite máximo permitido de R$21.474.836,47"
            );
        }

        return cents.intValue();
    }

    /**
     * Converte um valor em centavos (Integer) para BigDecimal decimal.
     * <p>
     * Esta é a operação reversa de convertToIntegerCents.
     *
     * @param cents Valor em centavos (ex: 10050)
     * @return Valor decimal em reais (ex: 100.50)
     * @throws IllegalArgumentException se cents for null ou negativo
     */
    public static BigDecimal convertToBigDecimal(Integer cents) {
        if (cents == null) {
            throw new IllegalArgumentException("O valor em centavos não pode ser nulo");
        }

        if (cents < 0) {
            throw new IllegalArgumentException("O valor em centavos não pode ser negativo");
        }

        return new BigDecimal(cents).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
    }
}

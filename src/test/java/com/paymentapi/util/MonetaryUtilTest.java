package com.paymentapi.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * Testes unitários para MonetaryUtil.
 * Valida conversões entre BigDecimal e Integer
 */
class MonetaryUtilTest {

    @Test
    void testConvertToIntegerCents() {
        // Arrange
        BigDecimal value = new BigDecimal("100.50");

        // Act
        Integer cents = MonetaryUtil.convertToIntegerCents(value);

        // Assert
        assertThat(cents).isEqualTo(10050);
    }

    @Test
    void testConvertToIntegerCentsRoundingHalfUp() {
        // Arrange - valor que arredonda para cima
        BigDecimal value = new BigDecimal("100.505");

        // Act
        Integer cents = MonetaryUtil.convertToIntegerCents(value);

        // Assert
        assertThat(cents).isEqualTo(10051);
    }

    @Test
    void testConvertToIntegerCentsRoundingDown() {
        // Arrange - valor que arredonda para baixo
        BigDecimal value = new BigDecimal("100.504");

        // Act
        Integer cents = MonetaryUtil.convertToIntegerCents(value);

        // Assert
        assertThat(cents).isEqualTo(10050);
    }

    @Test
    void testConvertToIntegerCentsNegative() {
        // Arrange
        BigDecimal value = new BigDecimal("-100.50");

        // Act & Assert
        assertThatThrownBy(() -> MonetaryUtil.convertToIntegerCents(value))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("O valor deve ser positivo");
    }

    @Test
    void testConvertToIntegerCentsZero() {
        // Arrange
        BigDecimal value = BigDecimal.ZERO;

        // Act & Assert
        assertThatThrownBy(() -> MonetaryUtil.convertToIntegerCents(value))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("O valor deve ser positivo");
    }

    @Test
    void testConvertToIntegerCentsExceedsMax() {
        // Arrange - valor que excede Integer.MAX_VALUE em centavos
        // Integer.MAX_VALUE = 2147483647 centavos = 21474836.47 reais
        BigDecimal value = new BigDecimal("21474836.48");

        // Act & Assert
        assertThatThrownBy(() -> MonetaryUtil.convertToIntegerCents(value))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("O valor excede o limite máximo permitido de R$21.474.836,47");
    }

    @Test
    void testConvertToIntegerCentsNull() {
        // Act & Assert
        assertThatThrownBy(() -> MonetaryUtil.convertToIntegerCents(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("O valor não pode ser nulo");
    }

    @Test
    void testConvertToBigDecimal() {
        // Arrange
        Integer cents = 10050;

        // Act
        BigDecimal value = MonetaryUtil.convertToBigDecimal(cents);

        // Assert
        assertThat(value).isEqualByComparingTo("100.50");
    }

    @Test
    void testConvertToBigDecimalNull() {
        // Act & Assert
        assertThatThrownBy(() -> MonetaryUtil.convertToBigDecimal(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("O valor em centavos não pode ser nulo");
    }

    @Test
    void testConvertToBigDecimalNegative() {
        // Arrange
        Integer cents = -10050;

        // Act & Assert
        assertThatThrownBy(() -> MonetaryUtil.convertToBigDecimal(cents))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("O valor em centavos não pode ser negativo");
    }

    @Test
    void testBoundaryValueOne() {
        // Arrange - menor valor possível: 1 centavo
        BigDecimal value = new BigDecimal("0.01");

        // Act
        Integer cents = MonetaryUtil.convertToIntegerCents(value);

        // Assert
        assertThat(cents).isEqualTo(1);
    }

    @Test
    void testRoundTripConversion() {
        // Arrange
        BigDecimal original = new BigDecimal("100.50");

        // Act - converte para centavos e volta
        Integer cents = MonetaryUtil.convertToIntegerCents(original);
        BigDecimal result = MonetaryUtil.convertToBigDecimal(cents);

        // Assert
        assertThat(result).isEqualByComparingTo(original);
    }

    @Test
    void testMaxValidValue() {
        // Arrange - valor máximo que cabe em Integer.MAX_VALUE centavos
        BigDecimal value = new BigDecimal("21474836.47");

        // Act
        Integer cents = MonetaryUtil.convertToIntegerCents(value);

        // Assert
        assertThat(cents).isEqualTo(Integer.MAX_VALUE);
    }
}

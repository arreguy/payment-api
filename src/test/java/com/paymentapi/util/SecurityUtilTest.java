package com.paymentapi.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testes unitários para SecurityUtil.
 * Valida o comportamento de mascaramento de CPF.
 */
class SecurityUtilTest {

    @Test
    @DisplayName("Deve mascarar CPF válido mostrando apenas 3 primeiros dígitos")
    void testMaskCpf_withValidCpf_shouldMaskCorrectly() {
        // Arrange
        String cpf = "12345678909";

        // Act
        String masked = SecurityUtil.maskCpf(cpf);

        // Assert
        assertThat(masked).isEqualTo("123.***.***-**");
    }

    @Test
    @DisplayName("Deve retornar email sem máscara quando não for CPF")
    void testMaskCpf_withEmail_shouldReturnUnmasked() {
        // Arrange
        String email = "joao@example.com";

        // Act
        String result = SecurityUtil.maskCpf(email);

        // Assert
        assertThat(result).isEqualTo(email);
    }

    @Test
    @DisplayName("Deve retornar null quando valor for null")
    void testMaskCpf_withNull_shouldReturnNull() {
        // Arrange & Act
        String result = SecurityUtil.maskCpf(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Deve retornar valor original quando CPF tiver menos de 11 dígitos")
    void testMaskCpf_withIncompleteCpf_shouldReturnOriginal() {
        // Arrange
        String incompleteCpf = "123456789";

        // Act
        String result = SecurityUtil.maskCpf(incompleteCpf);

        // Assert
        assertThat(result).isEqualTo(incompleteCpf);
    }

    @Test
    @DisplayName("Deve retornar valor original quando CPF tiver mais de 11 dígitos")
    void testMaskCpf_withLongCpf_shouldReturnOriginal() {
        // Arrange
        String longCpf = "123456789012";

        // Act
        String result = SecurityUtil.maskCpf(longCpf);

        // Assert
        assertThat(result).isEqualTo(longCpf);
    }

    @Test
    @DisplayName("Deve retornar valor original quando contiver caracteres não numéricos")
    void testMaskCpf_withNonNumeric_shouldReturnOriginal() {
        // Arrange
        String cpfWithMask = "123.456.789-09";

        // Act
        String result = SecurityUtil.maskCpf(cpfWithMask);

        // Assert
        assertThat(result).isEqualTo(cpfWithMask);
    }

    @Test
    @DisplayName("Deve retornar string vazia quando valor for string vazia")
    void testMaskCpf_withEmptyString_shouldReturnEmpty() {
        // Arrange
        String empty = "";

        // Act
        String result = SecurityUtil.maskCpf(empty);

        // Assert
        assertThat(result).isEmpty();
    }
}

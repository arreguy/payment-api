package com.paymentapi.validation;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Testes unitários para CpfOrCnpjValidator.
 */
@ExtendWith(MockitoExtension.class)
class CpfOrCnpjValidatorTest {

    @Mock
    private ConstraintValidatorContext context;

    private CpfOrCnpjValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CpfOrCnpjValidator();
    }

    @Test
    void testValidCpf() {
        // Arrange
        String cpf = "12345678909"; // CPF válido

        // Act
        boolean result = validator.isValid(cpf, context);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void testValidCnpj() {
        // Arrange
        String cnpj = "11222333000181"; // CNPJ válido (14 dígitos)

        // Act
        boolean result = validator.isValid(cnpj, context);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void testInvalidCpfChecksum() {
        // Arrange
        String cpf = "12345678901"; // CPF com checksum inválido

        // Act
        boolean result = validator.isValid(cpf, context);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void testInvalidCnpjChecksum() {
        // Arrange
        String cnpj = "11222333000180"; // CNPJ com checksum inválido

        // Act
        boolean result = validator.isValid(cnpj, context);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void testNullInput() {
        // Act
        boolean result = validator.isValid(null, context);

        // Assert
        // Retorna true pois null deve ser tratado por @NotNull, não pelo validator customizado
        assertThat(result).isTrue();
    }

    @Test
    void testEmptyInput() {
        // Arrange
        String empty = "";

        // Act
        boolean result = validator.isValid(empty, context);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void testInvalidLength() {
        // Arrange - 12 dígitos (nem CPF nem CNPJ)
        String invalid = "123456789012";

        // Act
        boolean result = validator.isValid(invalid, context);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void testSanitizationWithFormatting() {
        // Arrange
        String formattedCpf = "123.456.789-09";

        // Act
        boolean result = validator.isValid(formattedCpf, context);

        // Assert
        assertThat(result).isTrue(); // Deve passar após sanitização
    }

    @Test
    void testSanitizationWithCnpjFormatting() {
        // Arrange - CNPJ formatado
        String formattedCnpj = "11.222.333/0001-81";

        // Act
        boolean result = validator.isValid(formattedCnpj, context);

        // Assert
        assertThat(result).isTrue(); // Deve passar após sanitização
    }

    @Test
    void testMaliciousInputSanitization() {
        // Arrange - tentativa de SQL injection
        String malicious = "'; DROP TABLE users; --";

        // Act
        boolean result = validator.isValid(malicious, context);

        // Assert
        assertThat(result).isFalse(); // Deve falhar após sanitização
    }

    @Test
    void testCpfWithAllSameDigits() {
        String cpf = "11111111111";

        // Act
        boolean result = validator.isValid(cpf, context);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void testCnpjWithAllSameDigits() {
        String cnpj = "11111111111111";

        // Act
        boolean result = validator.isValid(cnpj, context);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void testShortInput() {
        // Arrange
        String short_input = "123";

        // Act
        boolean result = validator.isValid(short_input, context);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void testLongInput() {
        // Arrange
        String long_input = "123456789012345";

        // Act
        boolean result = validator.isValid(long_input, context);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void testInputWithLetters() {
        // Arrange
        String withLetters = "ABC123DEF456GHI";

        // Act
        boolean result = validator.isValid(withLetters, context);

        // Assert
        assertThat(result).isFalse(); // Após sanitização, não terá comprimento válido
    }
}

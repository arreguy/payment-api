package com.paymentapi.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Testes unitários para TransferRequest DTO.
 * <p>
 * Valida todas as anotações de validação e casos extremos:
 * <ul>
 *   <li>Validação de campos obrigatórios (@NotNull)</li>
 *   <li>Validação de valor positivo (@Positive)</li>
 *   <li>Validação de formato de CPF do pagador (@Pattern, @ValidCpf)</li>
 *   <li>Validação de formato de CPF/CNPJ do recebedor (@Pattern, @ValidCpfOrCnpj)</li>
 *   <li>Sanitização de inputs maliciosos</li>
 * </ul>
 */
class TransferRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidTransferRequest() {
        // Arrange
        TransferRequest request = new TransferRequest(
            new BigDecimal("100.50"),
            "12345678909", // CPF válido
            "98765432100"  // CPF válido
        );

        // Act
        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void testNullValue() {
        // Arrange
        TransferRequest request = new TransferRequest(
            null,
            "12345678909",
            "98765432100"
        );

        // Act
        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("O valor da transferência é obrigatório");
    }

    @Test
    void testNegativeValue() {
        // Arrange
        TransferRequest request = new TransferRequest(
            new BigDecimal("-100.50"),
            "12345678909",
            "98765432100"
        );

        // Act
        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("O valor da transferência deve ser positivo");
    }

    @Test
    void testZeroValue() {
        // Arrange
        TransferRequest request = new TransferRequest(
            BigDecimal.ZERO,
            "12345678909",
            "98765432100"
        );

        // Act
        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("O valor da transferência deve ser positivo");
    }

    @Test
    void testInvalidPayerCpfFormat() {
        // Arrange - CPF com apenas 10 dígitos
        TransferRequest request = new TransferRequest(
            new BigDecimal("100.50"),
            "1234567890",
            "98765432100"
        );

        // Act
        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

        // Assert - pode gerar múltiplas violações (@Pattern e @ValidCpf)
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getMessage().equals("O CPF do pagador deve conter exatamente 11 dígitos"));
    }

    @Test
    void testInvalidPayerCpfChecksum() {
        // Arrange - CPF com checksum inválido
        TransferRequest request = new TransferRequest(
            new BigDecimal("100.50"),
            "12345678901", // checksum inválido
            "98765432100"
        );

        // Act
        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("CPF inválido");
    }

    @Test
    void testInvalidPayeeCpfFormat() {
        // Arrange - Payee com formato inválido (muito curto)
        TransferRequest request = new TransferRequest(
            new BigDecimal("100.50"),
            "12345678909",
            "123"
        );

        // Act
        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

        // Assert - pode gerar múltiplas violações (@Pattern e @ValidCpfOrCnpj)
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getMessage().equals("O CPF/CNPJ do recebedor deve conter 11 ou 14 dígitos"));
    }

    @Test
    void testValidPayeeCnpj() {
        // Arrange - Payee com CNPJ válido (14 dígitos)
        TransferRequest request = new TransferRequest(
            new BigDecimal("100.50"),
            "12345678909",
            "11222333000181" // CNPJ válido
        );

        // Act
        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void testMaliciousInputSanitization() {
        // Arrange - Input malicioso (tentativa de SQL injection)
        TransferRequest request = new TransferRequest(
            new BigDecimal("100.50"),
            "12345678909",
            "'; DROP TABLE users; --"
        );

        // Act
        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

        // Assert
        // A validação deve falhar pois o input não é um CPF/CNPJ válido
        assertThat(violations).isNotEmpty();
        // O validator CpfOrCnpj sanitiza e valida, rejeitando o input malicioso
    }

    @Test
    void testNullPayer() {
        // Arrange
        TransferRequest request = new TransferRequest(
            new BigDecimal("100.50"),
            null,
            "98765432100"
        );

        // Act
        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("O CPF do pagador é obrigatório");
    }

    @Test
    void testNullPayee() {
        // Arrange
        TransferRequest request = new TransferRequest(
            new BigDecimal("100.50"),
            "12345678909",
            null
        );

        // Act
        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

        // Assert - pode gerar múltiplas violações (@NotNull e @ValidCpfOrCnpj)
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getMessage().equals("O CPF/CNPJ do recebedor é obrigatório"));
    }
}

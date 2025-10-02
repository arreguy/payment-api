package com.paymentapi.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.paymentapi.validation.constraints.ValidCpf;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Testes unitários pra validação de CPF com a anotação @ValidCpf
 */
class CpfValidatorTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void testValidCpfUnformatted() {
    // Arrange
    TestEntity entity = new TestEntity("12345678909");

    // Act
    Set<ConstraintViolation<TestEntity>> violations = validator.validate(entity);

    // Assert
    assertThat(violations).isEmpty();
  }

  @Test
  void testValidCpfFormatted() {
    // Arrange
    TestEntity entity = new TestEntity("123.456.789-09");

    // Act
    Set<ConstraintViolation<TestEntity>> violations = validator.validate(entity);

    // Assert
    assertThat(violations).isEmpty();
  }

  @Test
  void testInvalidCpfChecksum() {
    // Arrange - valid format but wrong checksum digits
    TestEntity entity = new TestEntity("12345678900");

    // Act
    Set<ConstraintViolation<TestEntity>> violations = validator.validate(entity);

    // Assert
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("CPF inválido");
  }

  @Test
  void testCpfAllSameDigits() {
    // Arrange
    TestEntity entity = new TestEntity("11111111111");

    // Act
    Set<ConstraintViolation<TestEntity>> violations = validator.validate(entity);

    // Assert
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("CPF inválido");
  }

  @Test
  void testCpfTooShort() {
    // Arrange
    TestEntity entity = new TestEntity("123456789");

    // Act
    Set<ConstraintViolation<TestEntity>> violations = validator.validate(entity);

    // Assert
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("CPF inválido");
  }

  @Test
  void testCpfTooLong() {
    // Arrange
    TestEntity entity = new TestEntity("123456789012");

    // Act
    Set<ConstraintViolation<TestEntity>> violations = validator.validate(entity);

    // Assert
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("CPF inválido");
  }

  @Test
  void testCpfWithLetters() {
    // Arrange
    TestEntity entity = new TestEntity("123abc78909");

    // Act
    Set<ConstraintViolation<TestEntity>> violations = validator.validate(entity);

    // Assert
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("CPF inválido");
  }

  @Test
  void testNullCpf() {
    // Arrange
    TestEntity entity = new TestEntity(null);

    // Act
    Set<ConstraintViolation<TestEntity>> violations = validator.validate(entity);

    // Assert - retorna true pra null
    assertThat(violations).isEmpty();
  }

  @Test
  void testEmptyCpf() {
    // Arrange
    TestEntity entity = new TestEntity("");

    // Act
    Set<ConstraintViolation<TestEntity>> violations = validator.validate(entity);

    // Assert - retorna true pra empty
    assertThat(violations).isEmpty();
  }

  /**
   * Entity de teste com anotação @ValidCpf.
   */
  private static class TestEntity {
    @ValidCpf
    private final String cpf;

    TestEntity(String cpf) {
      this.cpf = cpf;
    }
  }
}

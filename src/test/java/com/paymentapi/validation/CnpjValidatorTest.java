package com.paymentapi.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.paymentapi.validation.constraints.ValidCnpj;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Testes unitários pra validação de CNPJ com a anotação @ValidCnpj
 */
class CnpjValidatorTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void testValidCnpjUnformatted() {
    // Arrange
    TestEntity entity = new TestEntity("11222333000181");

    // Act
    Set<ConstraintViolation<TestEntity>> violations = validator.validate(entity);

    // Assert
    assertThat(violations).isEmpty();
  }

  @Test
  void testValidCnpjFormatted() {
    // Arrange
    TestEntity entity = new TestEntity("11.222.333/0001-81");

    // Act
    Set<ConstraintViolation<TestEntity>> violations = validator.validate(entity);

    // Assert
    assertThat(violations).isEmpty();
  }

  @Test
  void testInvalidCnpjChecksum() {
    // Arrange
    TestEntity entity = new TestEntity("11222333000180");

    // Act
    Set<ConstraintViolation<TestEntity>> violations = validator.validate(entity);

    // Assert
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("CNPJ inválido");
  }

  @Test
  void testCnpjAllSameDigits() {
    // Arrange
    TestEntity entity = new TestEntity("00000000000000");

    // Act
    Set<ConstraintViolation<TestEntity>> violations = validator.validate(entity);

    // Assert
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("CNPJ inválido");
  }

  @Test
  void testCnpjTooShort() {
    // Arrange
    TestEntity entity = new TestEntity("1122233300018");

    // Act
    Set<ConstraintViolation<TestEntity>> violations = validator.validate(entity);

    // Assert
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("CNPJ inválido");
  }

  @Test
  void testCnpjTooLong() {
    // Arrange
    TestEntity entity = new TestEntity("112223330001811");

    // Act
    Set<ConstraintViolation<TestEntity>> violations = validator.validate(entity);

    // Assert
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("CNPJ inválido");
  }

  @Test
  void testCnpjWithLetters() {
    // Arrange
    TestEntity entity = new TestEntity("11abc333000181");

    // Act
    Set<ConstraintViolation<TestEntity>> violations = validator.validate(entity);

    // Assert
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("CNPJ inválido");
  }

  @Test
  void testNullCnpj() {
    // Arrange
    TestEntity entity = new TestEntity(null);

    // Act
    Set<ConstraintViolation<TestEntity>> violations = validator.validate(entity);

    // Assert - retorna true pra null
    assertThat(violations).isEmpty();
  }

  @Test
  void testEmptyCnpj() {
    // Arrange
    TestEntity entity = new TestEntity("");

    // Act
    Set<ConstraintViolation<TestEntity>> violations = validator.validate(entity);

    // Assert - retorna true pra empty
    assertThat(violations).isEmpty();
  }

  /**
   * Entity de teste com anotação @ValidCnpj.
   */
  private static class TestEntity {
    @ValidCnpj
    private final String cnpj;

    TestEntity(String cnpj) {
      this.cnpj = cnpj;
    }
  }
}

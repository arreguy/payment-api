package com.paymentapi.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.paymentapi.entity.enums.UserType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Testes unitários para validação da entity User */
class UserTest {

  private static Validator validator;

  @BeforeAll
  static void setUpValidator() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void testValidUser() {
    // Arrange
    User user = new User();
    user.setNomeCompleto("Breno Silva");
    user.setCpf("12345678901");
    user.setEmail("breno.pandino@gmail.com");
    user.setSenha("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
    user.setUserType(UserType.COMMON_USER);
    user.setWalletBalance(10000);

    // Act
    Set<ConstraintViolation<User>> violations = validator.validate(user);

    // Assert
    assertThat(violations).isEmpty();
  }

  @Test
  void testNullNomeCompleto() {
    // Arrange
    User user = new User();
    user.setNomeCompleto(null); // Invalid
    user.setCpf("12345678901");
    user.setEmail("breno.pandino@gmail.com");
    user.setSenha("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
    user.setUserType(UserType.COMMON_USER);
    user.setWalletBalance(10000);

    // Act
    Set<ConstraintViolation<User>> violations = validator.validate(user);

    // Assert
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).contains("must not be null");
    assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("nomeCompleto");
  }

  @Test
  void testNullCpf() {
    // Arrange
    User user = new User();
    user.setNomeCompleto("Breno Pandino");
    user.setCpf(null); // Invalid
    user.setEmail("breno.pandino@gmail.com");
    user.setSenha("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
    user.setUserType(UserType.COMMON_USER);
    user.setWalletBalance(10000);

    // Act
    Set<ConstraintViolation<User>> violations = validator.validate(user);

    // Assert
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).contains("must not be null");
    assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("cpf");
  }

  @Test
  void testInvalidEmail() {
    // Arrange
    User user = new User();
    user.setNomeCompleto("Breno Pandino");
    user.setCpf("12345678901");
    user.setEmail("email-invalido");
    user.setSenha("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
    user.setUserType(UserType.COMMON_USER);
    user.setWalletBalance(10000);

    // Act
    Set<ConstraintViolation<User>> violations = validator.validate(user);

    // Assert
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .containsAnyOf("must be a well-formed email address", "not a valid email");
    assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("email");
  }

  @Test
  void testNullSenha() {
    // Arrange
    User user = new User();
    user.setNomeCompleto("Breno Pandino");
    user.setCpf("12345678901");
    user.setEmail("breno.pandino@gmail.com");
    user.setSenha(null); // Invalid
    user.setUserType(UserType.COMMON_USER);
    user.setWalletBalance(10000);

    // Act
    Set<ConstraintViolation<User>> violations = validator.validate(user);

    // Assert
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).contains("must not be null");
    assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("senha");
  }

  @Test
  void testNullUserType() {
    // Arrange
    User user = new User();
    user.setNomeCompleto("Breno Pandino");
    user.setCpf("12345678901");
    user.setEmail("breno.pandino@gmail.com");
    user.setSenha("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
    user.setUserType(null); // Invalid
    user.setWalletBalance(10000);

    // Act
    Set<ConstraintViolation<User>> violations = validator.validate(user);

    // Assert
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).contains("must not be null");
    assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("userType");
  }

  @Test
  void testEqualsAndHashCode() {
    // Arrange
    User user1 = new User();
    user1.setNomeCompleto("Breno Pandino");
    user1.setCpf("12345678901");
    user1.setEmail("breno.pandino@gmail.com");
    user1.setSenha("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
    user1.setUserType(UserType.COMMON_USER);
    user1.setWalletBalance(10000);

    User user2 = new User();
    user2.setNomeCompleto("Camila Pandino");
    user2.setCpf("98765432109");
    user2.setEmail("camila.pandino@gmail.com");
    user2.setSenha("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
    user2.setUserType(UserType.MERCHANT);
    user2.setWalletBalance(5000);

    // Act & Assert - Users com IDs diferentes devem ser diferentes
    user1.setId(java.util.UUID.randomUUID());
    user2.setId(java.util.UUID.randomUUID());
    assertThat(user1).isNotEqualTo(user2);

    // Act & Assert - Users com mesmos IDs devem ser iguais
    user2.setId(user1.getId());
    assertThat(user1).isEqualTo(user2);
    assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
  }
}

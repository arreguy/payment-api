package com.paymentapi.util;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.paymentapi.exception.InvalidPasswordException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testes unitários para PasswordValidator.
 * Valida todos os requisitos de complexidade de senha.
 */
class PasswordValidatorTest {

    @Test
    @DisplayName("Deve validar senha com sucesso quando atende todos os requisitos")
    void testValidPasswordSuccess() {
        // Arrange
        String validPassword = "Password123";

        // Act & Assert
        assertThatCode(() -> PasswordValidator.validatePassword(validPassword))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lançar InvalidPasswordException quando senha tem menos de 8 caracteres")
    void testPasswordTooShort() {
        // Arrange
        String shortPassword = "Pass1"; // 5 caracteres

        // Act & Assert
        assertThatThrownBy(() -> PasswordValidator.validatePassword(shortPassword))
            .isInstanceOf(InvalidPasswordException.class)
            .hasMessageContaining("no mínimo 8 caracteres");
    }

    @Test
    @DisplayName("Deve lançar InvalidPasswordException quando senha tem mais de 100 caracteres")
    void testPasswordTooLong() {
        // Arrange
        String longPassword = "A1bcdefgh".repeat(12) + "A"; // 101 caracteres

        // Act & Assert
        assertThatThrownBy(() -> PasswordValidator.validatePassword(longPassword))
            .isInstanceOf(InvalidPasswordException.class)
            .hasMessageContaining("no máximo 100 caracteres");
    }

    @Test
    @DisplayName("Deve lançar InvalidPasswordException quando senha não tem letra maiúscula")
    void testPasswordNoUppercase() {
        // Arrange
        String noUppercasePassword = "lowercase123";

        // Act & Assert
        assertThatThrownBy(() -> PasswordValidator.validatePassword(noUppercasePassword))
            .isInstanceOf(InvalidPasswordException.class)
            .hasMessageContaining("letra maiúscula");
    }

    @Test
    @DisplayName("Deve lançar InvalidPasswordException quando senha não tem letra minúscula")
    void testPasswordNoLowercase() {
        // Arrange
        String noLowercasePassword = "UPPERCASE123";

        // Act & Assert
        assertThatThrownBy(() -> PasswordValidator.validatePassword(noLowercasePassword))
            .isInstanceOf(InvalidPasswordException.class)
            .hasMessageContaining("letra minúscula");
    }

    @Test
    @DisplayName("Deve lançar InvalidPasswordException quando senha não tem dígito")
    void testPasswordNoDigit() {
        // Arrange
        String noDigitPassword = "PasswordOnly";

        // Act & Assert
        assertThatThrownBy(() -> PasswordValidator.validatePassword(noDigitPassword))
            .isInstanceOf(InvalidPasswordException.class)
            .hasMessageContaining("dígito");
    }

    @Test
    @DisplayName("Deve validar senha com exatamente 8 caracteres (mínimo válido)")
    void testPasswordMinimumValid() {
        // Arrange
        String minimumPassword = "Abc12345"; // Exatamente 8 caracteres

        // Act & Assert
        assertThatCode(() -> PasswordValidator.validatePassword(minimumPassword))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve validar senha com caracteres especiais")
    void testPasswordWithSpecialChars() {
        // Arrange
        String passwordWithSpecialChars = "Pass123!@#";

        // Act & Assert
        assertThatCode(() -> PasswordValidator.validatePassword(passwordWithSpecialChars))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lançar InvalidPasswordException quando senha é nula")
    void testPasswordNull() {
        // Act & Assert
        assertThatThrownBy(() -> PasswordValidator.validatePassword(null))
            .isInstanceOf(InvalidPasswordException.class)
            .hasMessageContaining("nula");
    }

    @Test
    @DisplayName("Deve validar senha com exatamente 100 caracteres (máximo válido)")
    void testPasswordMaximumValid() {
        // Arrange
        String maximumPassword = "A1bcdefgh".repeat(11) + "A"; // 100 caracteres exatos

        // Act & Assert
        assertThatCode(() -> PasswordValidator.validatePassword(maximumPassword))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lançar InvalidPasswordException quando senha tem 7 caracteres")
    void testPasswordSevenCharacters() {
        // Arrange
        String sevenCharsPassword = "Pass123"; // 7 caracteres

        // Act & Assert
        assertThatThrownBy(() -> PasswordValidator.validatePassword(sevenCharsPassword))
            .isInstanceOf(InvalidPasswordException.class)
            .hasMessageContaining("no mínimo 8 caracteres");
    }

    @Test
    @DisplayName("Deve validar senha complexa com múltiplos requisitos")
    void testComplexPasswordValid() {
        // Arrange
        String complexPassword = "C0mpl3xP@ssw0rd!";

        // Act & Assert
        assertThatCode(() -> PasswordValidator.validatePassword(complexPassword))
            .doesNotThrowAnyException();
    }
}

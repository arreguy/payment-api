package com.paymentapi.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paymentapi.entity.User;
import com.paymentapi.entity.enums.UserType;
import com.paymentapi.exception.UserNotFoundException;
import com.paymentapi.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Testes unitários para ValidationService.
 */
@ExtendWith(MockitoExtension.class)
class ValidationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ValidationService validationService;

    @Test
    void testValidateUserExistsWithCpf() {
        // Arrange
        String cpf = "12345678909";
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setCpf(cpf);
        user.setUserType(UserType.COMMON_USER);

        when(userRepository.findByCpf(cpf)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThatCode(() -> validationService.validateUserExists(cpf))
            .doesNotThrowAnyException();

        verify(userRepository).findByCpf(cpf);
    }

    @Test
    void testValidateUserExistsWithCnpj() {
        // Arrange
        String cnpj = "11222333000181";
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setCpf(cnpj);
        user.setUserType(UserType.MERCHANT);

        when(userRepository.findByCpf(cnpj)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThatCode(() -> validationService.validateUserExists(cnpj))
            .doesNotThrowAnyException();

        verify(userRepository).findByCpf(cnpj);
    }

    @Test
    void testValidateUserNotFoundCpf() {
        // Arrange
        String cpf = "12345678909";
        when(userRepository.findByCpf(cpf)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> validationService.validateUserExists(cpf))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("Usuário não encontrado")
            .hasMessageContaining("123.***.***-**"); // Verifica que o CPF está mascarado

        verify(userRepository).findByCpf(cpf);
    }

    @Test
    void testValidateUserNotFoundCnpj() {
        // Arrange
        String cnpj = "11222333000181";
        when(userRepository.findByCpf(cnpj)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> validationService.validateUserExists(cnpj))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("Usuário não encontrado");
        // CNPJ não segue o pattern de CPF, então não é mascarado da mesma forma

        verify(userRepository).findByCpf(cnpj);
    }

    @Test
    void testValidateUserExistsCallsRepositoryOnce() {
        // Arrange
        String cpf = "98765432100";
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setCpf(cpf);

        when(userRepository.findByCpf(cpf)).thenReturn(Optional.of(user));

        // Act
        validationService.validateUserExists(cpf);

        // Assert - verifica que o repositório foi chamado exatamente uma vez
        verify(userRepository).findByCpf(cpf);
    }
}

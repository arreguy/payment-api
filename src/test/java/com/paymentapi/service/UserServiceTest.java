package com.paymentapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paymentapi.dto.request.UserCreateRequest;
import com.paymentapi.dto.response.UserResponse;
import com.paymentapi.entity.User;
import com.paymentapi.entity.enums.UserType;
import com.paymentapi.exception.DuplicateCpfException;
import com.paymentapi.exception.DuplicateEmailException;
import com.paymentapi.exception.InvalidPasswordException;
import com.paymentapi.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Testes unitários para UserService.
 * Utiliza Mockito para mockar dependências e testar lógica de negócio isoladamente.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserCreateRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new UserCreateRequest(
            "João da Silva",
            "12345678909",
            "joao@example.com",
            "Password123",
            UserType.COMMON_USER
        );
    }

    @Test
    @DisplayName("Deve criar usuário com sucesso quando todos os dados são válidos")
    void testCreateUserSuccess() {
        // Arrange
        when(userRepository.findByCpf(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");

        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setNomeCompleto(validRequest.nomeCompleto());
        savedUser.setCpf(validRequest.cpf());
        savedUser.setEmail(validRequest.email());
        savedUser.setSenha("$2a$10$hashedPassword");
        savedUser.setUserType(validRequest.userType());
        savedUser.setWalletBalance(0);
        savedUser.setCreatedAt(LocalDateTime.now());

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserResponse response = userService.createUser(validRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(savedUser.getId());
        assertThat(response.nomeCompleto()).isEqualTo(validRequest.nomeCompleto());
        assertThat(response.cpf()).isEqualTo(validRequest.cpf());
        assertThat(response.email()).isEqualTo(validRequest.email());
        assertThat(response.userType()).isEqualTo(UserType.COMMON_USER);
        assertThat(response.walletBalance()).isEqualTo(0);
        assertThat(response.createdAt()).isNotNull();

        verify(userRepository, times(1)).findByCpf(validRequest.cpf());
        verify(userRepository, times(1)).findByEmail(validRequest.email());
        verify(passwordEncoder, times(1)).encode(validRequest.senha());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar DuplicateCpfException quando CPF já existe")
    void testCreateUserDuplicateCpf() {
        // Arrange
        User existingUser = new User();
        existingUser.setCpf(validRequest.cpf());
        when(userRepository.findByCpf(validRequest.cpf())).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(validRequest))
            .isInstanceOf(DuplicateCpfException.class)
            .hasMessageContaining("CPF já cadastrado");

        verify(userRepository, times(1)).findByCpf(validRequest.cpf());
        verify(userRepository, never()).findByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar DuplicateEmailException quando email já existe")
    void testCreateUserDuplicateEmail() {
        // Arrange
        when(userRepository.findByCpf(validRequest.cpf())).thenReturn(Optional.empty());
        User existingUser = new User();
        existingUser.setEmail(validRequest.email());
        when(userRepository.findByEmail(validRequest.email())).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(validRequest))
            .isInstanceOf(DuplicateEmailException.class)
            .hasMessageContaining("Email já cadastrado");

        verify(userRepository, times(1)).findByCpf(validRequest.cpf());
        verify(userRepository, times(1)).findByEmail(validRequest.email());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar InvalidPasswordException quando senha tem menos de 8 caracteres")
    void testCreateUserInvalidPasswordTooShort() {
        // Arrange
        UserCreateRequest shortPasswordRequest = new UserCreateRequest(
            "João da Silva",
            "12345678909",
            "joao@example.com",
            "Pass1", // Apenas 5 caracteres
            UserType.COMMON_USER
        );

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(shortPasswordRequest))
            .isInstanceOf(InvalidPasswordException.class)
            .hasMessageContaining("no mínimo 8 caracteres");

        verify(userRepository, never()).findByCpf(anyString());
        verify(userRepository, never()).findByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar InvalidPasswordException quando senha não tem letra maiúscula")
    void testCreateUserInvalidPasswordNoUppercase() {
        // Arrange
        UserCreateRequest noUppercaseRequest = new UserCreateRequest(
            "João da Silva",
            "12345678909",
            "joao@example.com",
            "password123", // Sem maiúscula
            UserType.COMMON_USER
        );

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(noUppercaseRequest))
            .isInstanceOf(InvalidPasswordException.class)
            .hasMessageContaining("letra maiúscula");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar InvalidPasswordException quando senha não tem letra minúscula")
    void testCreateUserInvalidPasswordNoLowercase() {
        // Arrange
        UserCreateRequest noLowercaseRequest = new UserCreateRequest(
            "João da Silva",
            "12345678909",
            "joao@example.com",
            "PASSWORD123", // Sem minúscula
            UserType.COMMON_USER
        );

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(noLowercaseRequest))
            .isInstanceOf(InvalidPasswordException.class)
            .hasMessageContaining("letra minúscula");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar InvalidPasswordException quando senha não tem dígito")
    void testCreateUserInvalidPasswordNoDigit() {
        // Arrange
        UserCreateRequest noDigitRequest = new UserCreateRequest(
            "João da Silva",
            "12345678909",
            "joao@example.com",
            "PasswordOnly", // Sem dígito
            UserType.COMMON_USER
        );

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(noDigitRequest))
            .isInstanceOf(InvalidPasswordException.class)
            .hasMessageContaining("dígito");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve chamar BCryptPasswordEncoder.encode() com senha em texto plano")
    void testCreateUserPasswordHashed() {
        // Arrange
        when(userRepository.findByCpf(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");

        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setNomeCompleto(validRequest.nomeCompleto());
        savedUser.setCpf(validRequest.cpf());
        savedUser.setEmail(validRequest.email());
        savedUser.setSenha("$2a$10$hashedPassword");
        savedUser.setUserType(validRequest.userType());
        savedUser.setWalletBalance(0);
        savedUser.setCreatedAt(LocalDateTime.now());

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        userService.createUser(validRequest);

        // Assert - verifica que encode foi chamado com senha em plain text
        verify(passwordEncoder, times(1)).encode("Password123");

        // Verifica que a entity salva tem senha hasheada
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getSenha()).isEqualTo("$2a$10$hashedPassword");
    }

    @Test
    @DisplayName("Deve inicializar walletBalance com 0 para novo usuário")
    void testCreateUserWalletBalanceInitialized() {
        // Arrange
        when(userRepository.findByCpf(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");

        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setNomeCompleto(validRequest.nomeCompleto());
        savedUser.setCpf(validRequest.cpf());
        savedUser.setEmail(validRequest.email());
        savedUser.setSenha("$2a$10$hashedPassword");
        savedUser.setUserType(validRequest.userType());
        savedUser.setWalletBalance(0);
        savedUser.setCreatedAt(LocalDateTime.now());

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserResponse response = userService.createUser(validRequest);

        // Assert
        assertThat(response.walletBalance()).isEqualTo(0);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getWalletBalance()).isEqualTo(0);
    }
}

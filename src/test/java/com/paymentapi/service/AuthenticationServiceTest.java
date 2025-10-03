package com.paymentapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paymentapi.dto.internal.UserContext;
import com.paymentapi.dto.request.AuthenticationRequest;
import com.paymentapi.entity.User;
import com.paymentapi.entity.enums.UserType;
import com.paymentapi.exception.AuthenticationException;
import com.paymentapi.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Testes unitários para AuthenticationService.
 * Utiliza Mockito para mockar dependências e testar lógica de autenticação isoladamente.
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private static final String TEST_CPF = "12345678909";
    private static final String TEST_EMAIL = "joao@example.com";
    private static final String TEST_PASSWORD = "Password123";
    private static final String TEST_HASHED_PASSWORD = "$2a$10$hashedPassword";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setNomeCompleto("João da Silva");
        testUser.setCpf(TEST_CPF);
        testUser.setEmail(TEST_EMAIL);
        testUser.setSenha(TEST_HASHED_PASSWORD);
        testUser.setUserType(UserType.COMMON_USER);
        testUser.setWalletBalance(10000);
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve autenticar usuário com sucesso usando CPF")
    void testValidateUserSuccessWithCpf() {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest(TEST_CPF, TEST_PASSWORD);
        when(userRepository.findByCpf(TEST_CPF)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_PASSWORD, TEST_HASHED_PASSWORD)).thenReturn(true);

        // Act
        UserContext result = authenticationService.validateUser(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(testUser.getId());
        assertThat(result.nomeCompleto()).isEqualTo(testUser.getNomeCompleto());
        assertThat(result.email()).isEqualTo(testUser.getEmail());
        assertThat(result.userType()).isEqualTo(testUser.getUserType());
        assertThat(result.authenticatedAt()).isNotNull();

        verify(userRepository).findByCpf(TEST_CPF);
        verify(passwordEncoder).matches(TEST_PASSWORD, TEST_HASHED_PASSWORD);
    }

    @Test
    @DisplayName("Deve autenticar usuário com sucesso usando email")
    void testValidateUserSuccessWithEmail() {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest(TEST_EMAIL, TEST_PASSWORD);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_PASSWORD, TEST_HASHED_PASSWORD)).thenReturn(true);

        // Act
        UserContext result = authenticationService.validateUser(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(testUser.getId());
        assertThat(result.nomeCompleto()).isEqualTo(testUser.getNomeCompleto());
        assertThat(result.email()).isEqualTo(testUser.getEmail());
        assertThat(result.userType()).isEqualTo(testUser.getUserType());
        assertThat(result.authenticatedAt()).isNotNull();

        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(passwordEncoder).matches(TEST_PASSWORD, TEST_HASHED_PASSWORD);
    }

    @Test
    @DisplayName("Deve lançar AuthenticationException quando usuário não é encontrado por CPF")
    void testValidateUserNotFoundCpf() {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest(TEST_CPF, TEST_PASSWORD);
        when(userRepository.findByCpf(TEST_CPF)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.validateUser(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("User not found")
                .hasMessageContaining("123.***.***-**");

        verify(userRepository).findByCpf(TEST_CPF);
    }

    @Test
    @DisplayName("Deve lançar AuthenticationException quando usuário não é encontrado por email")
    void testValidateUserNotFoundEmail() {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest(TEST_EMAIL, TEST_PASSWORD);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.validateUser(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("User not found")
                .hasMessageContaining(TEST_EMAIL);

        verify(userRepository).findByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("Deve lançar AuthenticationException quando senha é inválida")
    void testValidateUserInvalidPassword() {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest(TEST_CPF, "WrongPassword");
        when(userRepository.findByCpf(TEST_CPF)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongPassword", TEST_HASHED_PASSWORD)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.validateUser(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid credentials")
                .hasMessageContaining("123.***.***-**");

        verify(userRepository).findByCpf(TEST_CPF);
        verify(passwordEncoder).matches("WrongPassword", TEST_HASHED_PASSWORD);
    }

    @Test
    @DisplayName("Deve garantir que UserContext não expõe senha")
    void testUserContextDoesNotExposePassword() {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest(TEST_CPF, TEST_PASSWORD);
        when(userRepository.findByCpf(TEST_CPF)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_PASSWORD, TEST_HASHED_PASSWORD)).thenReturn(true);

        // Act
        UserContext result = authenticationService.validateUser(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.toString()).doesNotContain(TEST_HASHED_PASSWORD);
        assertThat(result.toString()).doesNotContain("senha");

        // Verify that UserContext record doesn't have a senha field
        assertThat(result.getClass().getRecordComponents())
                .extracting("name")
                .doesNotContain("senha");
    }

    @Test
    @DisplayName("Deve garantir que UserContext não expõe walletBalance")
    void testUserContextDoesNotExposeWalletBalance() {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest(TEST_CPF, TEST_PASSWORD);
        when(userRepository.findByCpf(TEST_CPF)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_PASSWORD, TEST_HASHED_PASSWORD)).thenReturn(true);

        // Act
        UserContext result = authenticationService.validateUser(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.toString()).doesNotContain("10000");
        assertThat(result.toString()).doesNotContain("walletBalance");

        // Verify that UserContext record doesn't have a walletBalance field
        assertThat(result.getClass().getRecordComponents())
                .extracting("name")
                .doesNotContain("walletBalance");
    }
}

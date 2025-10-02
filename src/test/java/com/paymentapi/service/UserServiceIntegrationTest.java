package com.paymentapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.paymentapi.dto.request.UserCreateRequest;
import com.paymentapi.dto.response.UserResponse;
import com.paymentapi.entity.User;
import com.paymentapi.entity.enums.UserType;
import com.paymentapi.exception.DuplicateCpfException;
import com.paymentapi.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Testes de integração para UserService usando Spring Boot Test e TestContainers.
 * Valida comportamento end-to-end com banco de dados PostgreSQL real.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve criar usuário com sucesso e persistir no banco de dados")
    void testCreateUserIntegrationSuccess() {
        // Arrange
        UserCreateRequest request = new UserCreateRequest(
            "Maria Santos",
            "12345678909",
            "maria@example.com",
            "SecurePass123",
            UserType.COMMON_USER
        );

        // Act
        UserResponse response = userService.createUser(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.nomeCompleto()).isEqualTo("Maria Santos");
        assertThat(response.cpf()).isEqualTo("12345678909");
        assertThat(response.email()).isEqualTo("maria@example.com");
        assertThat(response.userType()).isEqualTo(UserType.COMMON_USER);
        assertThat(response.walletBalance()).isEqualTo(0);
        assertThat(response.createdAt()).isNotNull();

        // Verifica persistência no banco
        Optional<User> savedUser = userRepository.findById(response.id());
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getNomeCompleto()).isEqualTo("Maria Santos");
        assertThat(savedUser.get().getWalletBalance()).isEqualTo(0);
    }

    @Test
    @DisplayName("Deve fazer rollback da transação quando CPF duplicado é detectado")
    void testCreateUserRollbackOnDuplicate() {
        // Arrange - cria primeiro usuário
        UserCreateRequest firstRequest = new UserCreateRequest(
            "João Silva",
            "12345678909",
            "joao@example.com",
            "Password123",
            UserType.COMMON_USER
        );
        userService.createUser(firstRequest);

        // Tenta criar segundo usuário com mesmo CPF
        UserCreateRequest duplicateRequest = new UserCreateRequest(
            "Pedro Santos",
            "12345678909", // CPF duplicado
            "pedro@example.com",
            "Password456",
            UserType.MERCHANT
        );

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(duplicateRequest))
            .isInstanceOf(DuplicateCpfException.class);

        // Verifica que apenas o primeiro usuário existe no banco
        Optional<User> userWithDuplicateCpf = userRepository.findByCpf("12345678909");
        assertThat(userWithDuplicateCpf).isPresent();
        assertThat(userWithDuplicateCpf.get().getEmail()).isEqualTo("joao@example.com");

        // Verifica que segundo usuário não foi criado
        Optional<User> secondUser = userRepository.findByEmail("pedro@example.com");
        assertThat(secondUser).isEmpty();
    }

    @Test
    @DisplayName("Deve armazenar senha como hash BCrypt no banco de dados")
    void testPasswordHashedInDatabase() {
        // Arrange
        UserCreateRequest request = new UserCreateRequest(
            "Ana Costa",
            "11144477735", // CPF válido
            "ana@example.com",
            "MyPassword123",
            UserType.MERCHANT
        );

        // Act
        UserResponse response = userService.createUser(request);

        // Assert
        Optional<User> savedUser = userRepository.findById(response.id());
        assertThat(savedUser).isPresent();

        String storedPassword = savedUser.get().getSenha();
        // BCrypt hash começa com $2a$ ou $2b$ seguido do número de rounds
        assertThat(storedPassword).startsWith("$2a$");
        assertThat(storedPassword).hasSize(60); // BCrypt hash tem 60 caracteres

        // Verifica que senha original não está armazenada
        assertThat(storedPassword).isNotEqualTo("MyPassword123");

        // Verifica que hash pode ser validado com BCryptPasswordEncoder
        boolean matches = passwordEncoder.matches("MyPassword123", storedPassword);
        assertThat(matches).isTrue();
    }

    @Test
    @DisplayName("Deve retornar UserResponse sem campo senha")
    void testUserResponseExcludesSenha() {
        // Arrange
        UserCreateRequest request = new UserCreateRequest(
            "Carlos Oliveira",
            "52998224725", // CPF válido
            "carlos@example.com",
            "SecretPass123",
            UserType.COMMON_USER
        );

        // Act
        UserResponse response = userService.createUser(request);

        // Assert
        assertThat(response.id()).isNotNull();
        assertThat(response.nomeCompleto()).isNotNull();
        assertThat(response.cpf()).isNotNull();
        assertThat(response.email()).isNotNull();
        assertThat(response.userType()).isNotNull();
        assertThat(response.walletBalance()).isNotNull();
        assertThat(response.createdAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve criar usuário do tipo MERCHANT com sucesso")
    void testCreateMerchantUser() {
        // Arrange
        UserCreateRequest merchantRequest = new UserCreateRequest(
            "Loja ABC",
            "11222333496", // CPF válido
            "loja@example.com",
            "MerchantPass123",
            UserType.MERCHANT
        );

        // Act
        UserResponse response = userService.createUser(merchantRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.userType()).isEqualTo(UserType.MERCHANT);

        Optional<User> savedUser = userRepository.findById(response.id());
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getUserType()).isEqualTo(UserType.MERCHANT);
    }

    @Test
    @DisplayName("Deve garantir que cada usuário tem hash único mesmo com mesma senha")
    void testUniqueSaltPerPassword() {
        // Arrange - dois usuários com mesma senha
        UserCreateRequest request1 = new UserCreateRequest(
            "User One",
            "19119119100", // CPF válido
            "user1@example.com",
            "SamePassword123",
            UserType.COMMON_USER
        );

        UserCreateRequest request2 = new UserCreateRequest(
            "User Two",
            "26326326300", // CPF válido
            "user2@example.com",
            "SamePassword123", // Mesma senha
            UserType.COMMON_USER
        );

        // Act
        UserResponse response1 = userService.createUser(request1);
        UserResponse response2 = userService.createUser(request2);

        // Assert
        Optional<User> user1 = userRepository.findById(response1.id());
        Optional<User> user2 = userRepository.findById(response2.id());

        assertThat(user1).isPresent();
        assertThat(user2).isPresent();

        // Hashes devem ser diferentes devido ao salting
        String hash1 = user1.get().getSenha();
        String hash2 = user2.get().getSenha();
        assertThat(hash1).isNotEqualTo(hash2);

        assertThat(passwordEncoder.matches("SamePassword123", hash1)).isTrue();
        assertThat(passwordEncoder.matches("SamePassword123", hash2)).isTrue();
    }
}

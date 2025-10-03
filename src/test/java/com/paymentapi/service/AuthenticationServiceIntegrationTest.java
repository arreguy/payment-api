package com.paymentapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.paymentapi.dto.internal.UserContext;
import com.paymentapi.dto.request.AuthenticationRequest;
import com.paymentapi.entity.User;
import com.paymentapi.entity.enums.UserType;
import com.paymentapi.exception.AuthenticationException;
import com.paymentapi.repository.UserRepository;
import com.paymentapi.util.CorrelationIdUtil;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Testes de integração para AuthenticationService usando Spring Boot Test e TestContainers.
 * Valida comportamento end-to-end com banco de dados PostgreSQL real e verificação de logs.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthenticationServiceIntegrationTest {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private ListAppender<ILoggingEvent> logAppender;
    private Logger logger;

    private static final String TEST_CPF = "12345678909";
    private static final String TEST_EMAIL = "joao@example.com";
    private static final String TEST_PASSWORD = "Password123";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        CorrelationIdUtil.clearCorrelationId();

        // Log appender pra capturar os logs
        logger = (Logger) LoggerFactory.getLogger(AuthenticationService.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        CorrelationIdUtil.clearCorrelationId();
        if (logger != null && logAppender != null) {
            logger.detachAppender(logAppender);
        }
    }

    @Test
    @DisplayName("Deve autenticar usuário end-to-end com CPF")
    void testAuthenticateUserEndToEndWithCpf() {
        // Arrange - cria usuário real no banco
        User user = createTestUser(TEST_CPF, TEST_EMAIL, TEST_PASSWORD);

        AuthenticationRequest request = new AuthenticationRequest(TEST_CPF, TEST_PASSWORD);

        // Act
        UserContext result = authenticationService.validateUser(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(user.getId());
        assertThat(result.nomeCompleto()).isEqualTo(user.getNomeCompleto());
        assertThat(result.email()).isEqualTo(user.getEmail());
        assertThat(result.userType()).isEqualTo(UserType.COMMON_USER);
        assertThat(result.authenticatedAt()).isNotNull();
        assertThat(result.authenticatedAt()).isBefore(LocalDateTime.now().plusSeconds(1));

        // Verify log de autenticação bem-sucedida
        List<ILoggingEvent> logEvents = logAppender.list;
        assertThat(logEvents).isNotEmpty();
        assertThat(logEvents)
                .anyMatch(event ->
                        event.getMessage().contains("Autenticação bem-sucedida")
                                && event.getMessage().contains("UserId"));
    }

    @Test
    @DisplayName("Deve autenticar usuário end-to-end com email")
    void testAuthenticateUserEndToEndWithEmail() {
        // Arrange - cria usuário real no banco
        User user = createTestUser(TEST_CPF, TEST_EMAIL, TEST_PASSWORD);

        AuthenticationRequest request = new AuthenticationRequest(TEST_EMAIL, TEST_PASSWORD);

        // Act
        UserContext result = authenticationService.validateUser(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(user.getId());
        assertThat(result.nomeCompleto()).isEqualTo(user.getNomeCompleto());
        assertThat(result.email()).isEqualTo(user.getEmail());
        assertThat(result.userType()).isEqualTo(UserType.COMMON_USER);
        assertThat(result.authenticatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve registrar falha de autenticação com correlation ID")
    void testFailedAuthenticationLogging() {
        // Arrange
        String correlationId = "test-correlation-id-123";
        CorrelationIdUtil.setCorrelationId(correlationId);

        AuthenticationRequest request = new AuthenticationRequest(TEST_CPF, TEST_PASSWORD);

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.validateUser(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("User not found");

        // Verify se log contém correlation ID e username mascarado
        List<ILoggingEvent> logEvents = logAppender.list;
        assertThat(logEvents).isNotEmpty();

        boolean foundFailureLog = logEvents.stream()
                .anyMatch(event ->
                        event.getMessage().contains("Tentativa de autenticação falhou")
                                && event.getFormattedMessage().contains(correlationId)
                                && event.getFormattedMessage().contains("123.***.***-**"));

        assertThat(foundFailureLog)
                .as("Log de falha de autenticação deve conter correlation ID e CPF mascarado")
                .isTrue();
    }

    @Test
    @DisplayName("Deve verificar senha usando BCrypt corretamente")
    void testPasswordVerificationWithBCrypt() {
        // Arrange - cria usuário com senha BCrypt
        User user = createTestUser(TEST_CPF, TEST_EMAIL, TEST_PASSWORD);

        // Verifica que a senha foi hasheada
        assertThat(user.getSenha()).isNotEqualTo(TEST_PASSWORD);
        assertThat(user.getSenha()).startsWith("$2a$");
        assertThat(user.getSenha()).hasSize(60);

        // Act - tenta autenticar com senha correta
        AuthenticationRequest correctRequest = new AuthenticationRequest(TEST_CPF, TEST_PASSWORD);
        UserContext result = authenticationService.validateUser(correctRequest);

        // Assert - autenticação bem-sucedida
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(user.getId());

        // Act - tenta autenticar com senha incorreta
        AuthenticationRequest wrongRequest = new AuthenticationRequest(TEST_CPF, "WrongPassword123");

        // Assert - autenticação falha
        assertThatThrownBy(() -> authenticationService.validateUser(wrongRequest))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    @DisplayName("Deve registrar tentativa de autenticação com senha inválida")
    void testFailedAuthenticationWithInvalidPassword() {
        // Arrange - cria usuário real
        User user = createTestUser(TEST_CPF, TEST_EMAIL, TEST_PASSWORD);

        String correlationId = "test-correlation-id-456";
        CorrelationIdUtil.setCorrelationId(correlationId);

        AuthenticationRequest request = new AuthenticationRequest(TEST_CPF, "WrongPassword");

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.validateUser(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid credentials");

        // Verify que log contém correlation ID, UserId e indica credenciais inválidas
        List<ILoggingEvent> logEvents = logAppender.list;
        assertThat(logEvents).isNotEmpty();

        boolean foundFailureLog = logEvents.stream()
                .anyMatch(event ->
                        event.getMessage().contains("credenciais inválidas")
                                && event.getFormattedMessage().contains(correlationId)
                                && event.getFormattedMessage().contains(user.getId().toString()));

        assertThat(foundFailureLog)
                .as("Log deve conter correlation ID e UserId para senha inválida")
                .isTrue();
    }

    /**
     * Metodo helper para criar usuário de teste no banco de dados.
     */
    private User createTestUser(String cpf, String email, String rawPassword) {
        User user = new User();
        user.setNomeCompleto("João da Silva");
        user.setCpf(cpf);
        user.setEmail(email);
        user.setSenha(passwordEncoder.encode(rawPassword));
        user.setUserType(UserType.COMMON_USER);
        user.setWalletBalance(10000);
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }
}

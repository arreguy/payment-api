package com.paymentapi.service;

import com.paymentapi.dto.internal.UserContext;
import com.paymentapi.dto.request.AuthenticationRequest;
import com.paymentapi.entity.User;
import com.paymentapi.exception.AuthenticationException;
import com.paymentapi.repository.UserRepository;
import com.paymentapi.util.CorrelationIdUtil;
import com.paymentapi.util.SecurityUtil;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço responsável pela autenticação de usuários no sistema de pagamento.
 *  - Identificação do tipo de username (CPF ou email)
 *  - Busca do usuário no banco de dados
 *  - Verificação de senha usando BCrypt
 *  - Geração de contexto do usuário autenticado
 *  - Logging de tentativas de autenticação falhas para auditoria de segurança
 */
@Service
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthenticationService(
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Valida as credenciais do usuário e retorna o contexto do usuário autenticado.
     * Tentativas de autenticação falhas são registradas nos logs com correlation ID
     *
     * @param request objeto contendo username (CPF ou email) e senha
     * @return UserContext contendo informações do usuário autenticado sem dados sensíveis
     * @throws AuthenticationException se o usuário não for encontrado ou a senha for inválida
     */
    @Transactional(readOnly = true)
    public UserContext validateUser(AuthenticationRequest request) {
        String username = request.username();
        String password = request.password();

        String correlationId = CorrelationIdUtil.getCorrelationId();
        if (correlationId == null) {
            correlationId = CorrelationIdUtil.generateCorrelationId();
            CorrelationIdUtil.setCorrelationId(correlationId);
        }

        // Determina se username é CPF (11 dígitos) ou email (tem @)
        boolean isCpf = username.matches("\\d{11}");
        Optional<User> userOptional;

        if (isCpf) {
            userOptional = userRepository.findByCpf(username);
        } else {
            userOptional = userRepository.findByEmail(username);
        }

        // Valida se usuário existe
        if (userOptional.isEmpty()) {
            logger.warn(
                    "Tentativa de autenticação falhou - usuário não encontrado. Username: {}, CorrelationId: {}",
                    SecurityUtil.maskCpf(username),
                    correlationId);
            throw new AuthenticationException(username, "User not found");
        }

        User user = userOptional.get();

        // Verifica senha usando BCrypt
        if (!passwordEncoder.matches(password, user.getSenha())) {
            logger.warn(
                    "Tentativa de autenticação falhou - credenciais inválidas. Username: {}, UserId: {}, CorrelationId: {}",
                    SecurityUtil.maskCpf(username),
                    user.getId(),
                    correlationId);
            throw new AuthenticationException(username, "Invalid credentials");
        }

        // Autenticação bem-sucedida: cria e retorna UserContext
        logger.info(
                "Autenticação bem-sucedida. UserId: {}, UserType: {}, CorrelationId: {}",
                user.getId(),
                user.getUserType(),
                correlationId);

        return new UserContext(
                user.getId(),
                user.getNomeCompleto(),
                user.getEmail(),
                user.getUserType(),
                LocalDateTime.now());
    }
}

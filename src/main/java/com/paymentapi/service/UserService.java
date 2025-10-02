package com.paymentapi.service;

import com.paymentapi.dto.request.UserCreateRequest;
import com.paymentapi.dto.response.UserResponse;
import com.paymentapi.entity.User;
import com.paymentapi.exception.DuplicateCpfException;
import com.paymentapi.exception.DuplicateEmailException;
import com.paymentapi.repository.UserRepository;
import com.paymentapi.util.PasswordValidator;
import java.time.LocalDateTime;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço de gerenciamento de usuários do sistema de pagamento.
 * Responsável por operações de criação, autenticação e gerenciamento de carteira de usuários.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Construtor com injeção de dependência
     *
     * @param userRepository repositório para acesso a dados de usuários
     * @param passwordEncoder encoder BCrypt para hashing de senhas
     */
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Cria um novo usuário no sistema com validações completas de segurança usando @Transactional.
     *
     * @param request dados para criação do usuário
     * @return UserResponse com dados do usuário criado (sem senha)
     * @throws InvalidPasswordException se senha não for complexa o suficiente
     * @throws DuplicateCpfException se CPF já estiver cadastrado
     * @throws DuplicateEmailException se e-mail já estiver cadastrado
     */
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        // Valida complexidade da senha antes de hashear
        PasswordValidator.validatePassword(request.senha());

        if (userRepository.findByCpf(request.cpf()).isPresent()) {
            throw new DuplicateCpfException("CPF já cadastrado: " + request.cpf());
        }

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new DuplicateEmailException("Email já cadastrado: " + request.email());
        }

        // Hash da senha
        String hashedPassword = passwordEncoder.encode(request.senha());

        // Cria entidade User
        User user = new User();
        user.setNomeCompleto(request.nomeCompleto());
        user.setCpf(request.cpf());
        user.setEmail(request.email());
        user.setSenha(hashedPassword);
        user.setUserType(request.userType());
        user.setWalletBalance(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // Retorna DTO
        return new UserResponse(
            savedUser.getId(),
            savedUser.getNomeCompleto(),
            savedUser.getCpf(),
            savedUser.getEmail(),
            savedUser.getUserType(),
            savedUser.getWalletBalance(),
            savedUser.getCreatedAt()
        );
    }
}

package com.paymentapi.service;

import com.paymentapi.exception.UserNotFoundException;
import com.paymentapi.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço de validação de regras de negócio.
 * <p>
 * Este serviço centraliza validações que requerem acesso ao banco de dados,
 * como verificação de existência de usuários para operações de transferência.
 */
@Service
public class ValidationService {

    private final UserRepository userRepository;

    /**
     * Construtor com injeção de dependências.
     *
     * @param userRepository repositório de usuários
     */
    public ValidationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Valida se um usuário existe no sistema baseado em CPF ou CNPJ.
     * <p>
     * Este método detecta automaticamente se o input é CPF (11 dígitos)
     * ou CNPJ (14 dígitos) e realiza a busca apropriada. Ambos são armazenados
     * no campo CPF da entidade User.
     * <p>
     * Utiliza @Transactional(readOnly = true) para leituras consistentes
     * sem necessidade de locks de escrita.
     *
     * @param cpfOrCnpj CPF (11 dígitos) ou CNPJ (14 dígitos) do usuário
     * @throws UserNotFoundException se o usuário não for encontrado
     */
    @Transactional(readOnly = true)
    public void validateUserExists(String cpfOrCnpj) {
        // CPF e CNPJ são ambos armazenados no campo CPF
        // A distinção é feita pelo comprimento: 11 = CPF, 14 = CNPJ
        userRepository.findByCpf(cpfOrCnpj)
            .orElseThrow(() -> new UserNotFoundException(cpfOrCnpj));
    }
}

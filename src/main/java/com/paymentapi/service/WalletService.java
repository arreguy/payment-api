package com.paymentapi.service;

import com.paymentapi.dto.response.WalletBalanceResponse;
import com.paymentapi.entity.User;
import com.paymentapi.entity.WalletBalanceAudit;
import com.paymentapi.entity.enums.UserType;
import com.paymentapi.exception.InsufficientFundsException;
import com.paymentapi.exception.NegativeBalanceException;
import com.paymentapi.exception.UserNotFoundException;
import com.paymentapi.repository.UserRepository;
import com.paymentapi.repository.WalletBalanceAuditRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço responsável por operações de controle de saldo da carteira.
 * - Pegar o saldo de carteira dos usuários
 * - Validação de fundos suficientes para operações
 * - Atualização atômica de saldos
 */
@Service
public class WalletService {

    private final UserRepository userRepository;
    private final WalletBalanceAuditRepository walletBalanceAuditRepository;

    /**
     * Construtor do Wallet Service
     *
     * @param userRepository repositório para acesso dos dados
     * @param walletBalanceAuditRepository repositório para os registros das transações
     */
    public WalletService(UserRepository userRepository,
                         WalletBalanceAuditRepository walletBalanceAuditRepository) {
        this.userRepository = userRepository;
        this.walletBalanceAuditRepository = walletBalanceAuditRepository;
    }

    /**
     * Pega o saldo atual da carteira de um usuário.
     *
     * @param userId UUID do user
     * @return WalletBalanceResponse com userId, walletBalance e timestamp lastUpdated
     * @throws UserNotFoundException se o user com o ID não existir
     */
    @Transactional(readOnly = true)
    public WalletBalanceResponse getBalance(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        return new WalletBalanceResponse(
            user.getId(),
            user.getWalletBalance(),
            user.getUpdatedAt()
        );
    }

    /**
     * Valida que o user tem fundos suficientes para a transação de débito.
     *
     * @param userId UUID do user
     * @param amount valor requerido (deve ser positivo)
     * @throws UserNotFoundException se o user com o ID não existir
     * @throws InsufficientFundsException se o saldo atual for menor que o valor requerido
     */
    @Transactional(readOnly = true)
    public void validateSufficientFunds(UUID userId, Integer amount) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        if (user.getWalletBalance() < amount) {
            throw new InsufficientFundsException(userId, amount, user.getWalletBalance());
        }
    }

    /**
     * Atualiza atomicamente o saldo da carteira de um user com criação de registros.
     * Todas as operações são atômicas em uma única transação.
     *
     * @param userId UUID do user
     * @param balanceChange mudança no saldo (positiva pra crédito, negativa pra débito)
     * @param operationType o tipo da operação (exemplo: TRANSFER_DEBIT, TRANSFER_CREDIT)
     * @param transactionId o UUID da transação associada
     * @return WalletBalanceResponse com informação atualizada de saldo
     * @throws UserNotFoundException se o user com o ID dado não existir
     * @throws NegativeBalanceException se o saldo do user se tornaria negativo
     */
    @Transactional
    public WalletBalanceResponse updateBalance(UUID userId, Integer balanceChange,
                                                String operationType, UUID transactionId) {
        // Pessimist locking
        User user = userRepository.findByIdForUpdate(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        // Calcular novo saldo
        Integer previousBalance = user.getWalletBalance();
        Integer newBalance = previousBalance + balanceChange;

        // Validar constraints de saldo com base no user type
        if (user.getUserType() == UserType.COMMON_USER && newBalance < 0) {
            throw new NegativeBalanceException(userId, newBalance);
        }

        // Atualizar o saldo do user
        user.setWalletBalance(newBalance);
        userRepository.save(user);

        // Criar registro de auditoria
        WalletBalanceAudit auditRecord = new WalletBalanceAudit();
        auditRecord.setUserId(userId);
        auditRecord.setTransactionId(transactionId);
        auditRecord.setPreviousBalance(previousBalance);
        auditRecord.setNewBalance(newBalance);
        auditRecord.setBalanceChange(balanceChange);
        auditRecord.setOperationType(operationType);
        auditRecord.setCreatedBy("system");
        walletBalanceAuditRepository.save(auditRecord);

        return new WalletBalanceResponse(
            user.getId(),
            user.getWalletBalance(),
            user.getUpdatedAt()
        );
    }
}

package com.paymentapi.repository;

import com.paymentapi.entity.WalletBalanceAudit;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório para as operações da entidade WalletBalanceAudit
 * Fornece os métodos de acesso aos dados pra consultar os registros de transações
 */
@Repository
public interface WalletBalanceAuditRepository extends JpaRepository<WalletBalanceAudit, UUID> {

    /**
     * Busca todos os registros de transação de um usuário específico, ordenados pela data de criação
     * Retorna os mais recentes primeiro.
     *
     * @param userId o UUID do usuário
     * @return lista de registros de transação rodenados por createdAt DESC
     */
    List<WalletBalanceAudit> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Busca todos os registros de auditoria associados a uma transação específica.
     *
     * Esperado que retorne 2 registros para 1 transferência, 1 para o pagador (débito)
     * e outra para o recebedor (crédito).
     *
     * @param transactionId UUID da transação
     * @return lista com os registros da transação
     */
    List<WalletBalanceAudit> findByTransactionId(UUID transactionId);
}

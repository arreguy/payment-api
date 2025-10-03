package com.paymentapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Entidade JPA que representa um registro de auditoria para mudanças de saldo de carteira.
 * Este registro mantém apenas os UUIDs de referência (userId e transactionId)
 */
@Entity
@Table(
    name = "balance_audit",
    indexes = {
      @Index(name = "idx_balance_audit_user_id", columnList = "user_id"),
      @Index(name = "idx_balance_audit_transaction_id", columnList = "transaction_id"),
      @Index(name = "idx_balance_audit_created_at", columnList = "created_at")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class WalletBalanceAudit {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @EqualsAndHashCode.Include
  private UUID id;

  @NotNull
  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "transaction_id")
  private UUID transactionId;

  @NotNull
  @Column(name = "previous_balance", nullable = false)
  private Integer previousBalance;

  @NotNull
  @Column(name = "new_balance", nullable = false)
  private Integer newBalance;

  @NotNull
  @Column(name = "balance_change", nullable = false)
  private Integer balanceChange;

  @NotNull
  @Column(name = "operation_type", length = 50, nullable = false)
  private String operationType;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @NotNull
  @Column(name = "created_by", length = 50, nullable = false)
  private String createdBy = "system";
}

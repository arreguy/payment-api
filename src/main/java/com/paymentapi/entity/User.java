package com.paymentapi.entity;

import com.paymentapi.entity.enums.UserType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Entidade JPA que representa um usuário no sistema de pagamento.
 * User pode ser COMMON_USER (envia e recebe) ou MERCHANT (só pode receber)
 */
@Entity
@Table(
    name = "users",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_users_cpf", columnNames = "cpf"),
      @UniqueConstraint(name = "uk_users_email", columnNames = "email")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @EqualsAndHashCode.Include
  private UUID id;

  @NotNull
  @Column(name = "nome_completo", length = 100, nullable = false)
  private String nomeCompleto;

  @NotNull
  // @ValidCpf - Placeholder a ser desenvolvido
  @Column(unique = true, length = 11, nullable = false)
  private String cpf;

  @NotNull
  @Email
  @Column(unique = true, length = 255, nullable = false)
  private String email;

  @NotNull
  @Column(length = 60, nullable = false)
  private String senha;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "user_type", length = 20, nullable = false)
  private UserType userType;

  @NotNull
  @Column(name = "wallet_balance", nullable = false)
  private Integer walletBalance = 0;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Version
  @Column(nullable = false)
  private Integer version = 0;
}

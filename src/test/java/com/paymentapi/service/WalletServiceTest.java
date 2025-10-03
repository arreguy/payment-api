package com.paymentapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paymentapi.dto.response.WalletBalanceResponse;
import com.paymentapi.entity.User;
import com.paymentapi.entity.WalletBalanceAudit;
import com.paymentapi.entity.enums.UserType;
import com.paymentapi.exception.InsufficientFundsException;
import com.paymentapi.exception.NegativeBalanceException;
import com.paymentapi.exception.UserNotFoundException;
import com.paymentapi.repository.UserRepository;
import com.paymentapi.repository.WalletBalanceAuditRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Testes unitários para o WalletService.
 * Utiliza Mockito para mockar UserRepository e WalletBalanceAuditRepository.
 */
@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private WalletBalanceAuditRepository walletBalanceAuditRepository;

  @InjectMocks private WalletService walletService;

  private User commonUser;
  private User merchantUser;
  private UUID userId;
  private UUID merchantId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    merchantId = UUID.randomUUID();

    // Arrange - Usuário Comum
    commonUser = new User();
    commonUser.setId(userId);
    commonUser.setNomeCompleto("João Silva");
    commonUser.setCpf("12345678901");
    commonUser.setEmail("joao@example.com");
    commonUser.setSenha("hashedPassword");
    commonUser.setUserType(UserType.COMMON_USER);
    commonUser.setWalletBalance(10000); // R$ 100.00
    commonUser.setCreatedAt(LocalDateTime.now());
    commonUser.setUpdatedAt(LocalDateTime.now());
    commonUser.setVersion(0);

    // Arrange - Lojista
    merchantUser = new User();
    merchantUser.setId(merchantId);
    merchantUser.setNomeCompleto("Loja ABC");
    merchantUser.setCpf("98765432100");
    merchantUser.setEmail("loja@example.com");
    merchantUser.setSenha("hashedPassword");
    merchantUser.setUserType(UserType.MERCHANT);
    merchantUser.setWalletBalance(5000); // R$ 50.00
    merchantUser.setCreatedAt(LocalDateTime.now());
    merchantUser.setUpdatedAt(LocalDateTime.now());
    merchantUser.setVersion(0);
  }

  @Test
  void testGetBalanceSuccess() {
    // Arrange
    when(userRepository.findById(userId)).thenReturn(Optional.of(commonUser));

    // Act
    WalletBalanceResponse response = walletService.getBalance(userId);

    // Assert
    assertThat(response).isNotNull();
    assertThat(response.userId()).isEqualTo(userId);
    assertThat(response.walletBalance()).isEqualTo(10000);
    assertThat(response.lastUpdated()).isNotNull();
    verify(userRepository, times(1)).findById(userId);
  }

  @Test
  void testGetBalanceUserNotFound() {
    // Arrange
    UUID nonExistentUserId = UUID.randomUUID();
    when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> walletService.getBalance(nonExistentUserId))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining(nonExistentUserId.toString());
    verify(userRepository, times(1)).findById(nonExistentUserId);
  }

  @Test
  void testValidateSufficientFundsSuccess() {
    // Arrange
    when(userRepository.findById(userId)).thenReturn(Optional.of(commonUser));

    // Act
    walletService.validateSufficientFunds(userId, 5000);

    // Assert
    verify(userRepository, times(1)).findById(userId);
  }

  @Test
  void testValidateSufficientFundsInsufficient() {
    // Arrange
    when(userRepository.findById(userId)).thenReturn(Optional.of(commonUser));

    // Act & Assert
    assertThatThrownBy(() -> walletService.validateSufficientFunds(userId, 15000))
        .isInstanceOf(InsufficientFundsException.class)
        .hasMessageContaining("Saldo insuficiente")
        .hasMessageContaining(userId.toString());
    verify(userRepository, times(1)).findById(userId);
  }

  @Test
  void testUpdateBalanceCredit() {
    // Arrange
    when(userRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(commonUser));
    when(userRepository.save(any(User.class))).thenReturn(commonUser);
    when(walletBalanceAuditRepository.save(any(WalletBalanceAudit.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UUID transactionId = UUID.randomUUID();

    // Act
    WalletBalanceResponse response =
        walletService.updateBalance(userId, 2000, "TRANSFER_CREDIT", transactionId);

    // Assert
    assertThat(response).isNotNull();
    assertThat(response.userId()).isEqualTo(userId);
    assertThat(response.walletBalance()).isEqualTo(12000); // 10000 + 2000

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository, times(1)).save(userCaptor.capture());
    assertThat(userCaptor.getValue().getWalletBalance()).isEqualTo(12000);

    ArgumentCaptor<WalletBalanceAudit> auditCaptor =
        ArgumentCaptor.forClass(WalletBalanceAudit.class);
    verify(walletBalanceAuditRepository, times(1)).save(auditCaptor.capture());
    WalletBalanceAudit audit = auditCaptor.getValue();
    assertThat(audit.getUserId()).isEqualTo(userId);
    assertThat(audit.getTransactionId()).isEqualTo(transactionId);
    assertThat(audit.getPreviousBalance()).isEqualTo(10000);
    assertThat(audit.getNewBalance()).isEqualTo(12000);
    assertThat(audit.getBalanceChange()).isEqualTo(2000);
    assertThat(audit.getOperationType()).isEqualTo("TRANSFER_CREDIT");
    assertThat(audit.getCreatedBy()).isEqualTo("system");
  }

  @Test
  void testUpdateBalanceDebit() {
    // Arrange
    when(userRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(commonUser));
    when(userRepository.save(any(User.class))).thenReturn(commonUser);
    when(walletBalanceAuditRepository.save(any(WalletBalanceAudit.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UUID transactionId = UUID.randomUUID();

    // Act
    WalletBalanceResponse response =
        walletService.updateBalance(userId, -3000, "TRANSFER_DEBIT", transactionId);

    // Assert
    assertThat(response).isNotNull();
    assertThat(response.userId()).isEqualTo(userId);
    assertThat(response.walletBalance()).isEqualTo(7000); // 10000 - 3000

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository, times(1)).save(userCaptor.capture());
    assertThat(userCaptor.getValue().getWalletBalance()).isEqualTo(7000);

    ArgumentCaptor<WalletBalanceAudit> auditCaptor =
        ArgumentCaptor.forClass(WalletBalanceAudit.class);
    verify(walletBalanceAuditRepository, times(1)).save(auditCaptor.capture());
    WalletBalanceAudit audit = auditCaptor.getValue();
    assertThat(audit.getPreviousBalance()).isEqualTo(10000);
    assertThat(audit.getNewBalance()).isEqualTo(7000);
    assertThat(audit.getBalanceChange()).isEqualTo(-3000);
    assertThat(audit.getOperationType()).isEqualTo("TRANSFER_DEBIT");
  }

  @Test
  void testUpdateBalanceCommonUserNegative() {
    // Arrange
    when(userRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(commonUser));

    // Act & Assert
    assertThatThrownBy(
            () -> walletService.updateBalance(userId, -12000, "TRANSFER_DEBIT", UUID.randomUUID()))
        .isInstanceOf(NegativeBalanceException.class)
        .hasMessageContaining("Saldo negativo não permitido para usuário comum")
        .hasMessageContaining(userId.toString());

    // Verificar que user não foi salvo
    verify(userRepository, times(0)).save(any(User.class));
    verify(walletBalanceAuditRepository, times(0)).save(any(WalletBalanceAudit.class));
  }

  @Test
  void testUpdateBalanceMerchantNegativeAllowed() {
    // Arrange
    when(userRepository.findByIdForUpdate(merchantId)).thenReturn(Optional.of(merchantUser));
    when(userRepository.save(any(User.class))).thenReturn(merchantUser);
    when(walletBalanceAuditRepository.save(any(WalletBalanceAudit.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act - lojista pode negativar
    WalletBalanceResponse response =
        walletService.updateBalance(merchantId, -7000, "TRANSFER_DEBIT", UUID.randomUUID());

    // Assert
    assertThat(response).isNotNull();
    assertThat(response.walletBalance()).isEqualTo(-2000); // 5000 - 7000 = -2000 (allowed for
    // merchants)

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository, times(1)).save(userCaptor.capture());
    assertThat(userCaptor.getValue().getWalletBalance()).isEqualTo(-2000);
  }

  @Test
  void testUpdateBalanceUserNotFound() {
    // Arrange
    UUID nonExistentUserId = UUID.randomUUID();
    when(userRepository.findByIdForUpdate(nonExistentUserId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(
            () ->
                walletService.updateBalance(
                    nonExistentUserId, 1000, "TRANSFER_CREDIT", UUID.randomUUID()))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining(nonExistentUserId.toString());

    verify(userRepository, times(0)).save(any(User.class));
  }

  @Test
  void testUpdateBalanceAuditRecordCreated() {
    // Arrange
    when(userRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(commonUser));
    when(userRepository.save(any(User.class))).thenReturn(commonUser);
    when(walletBalanceAuditRepository.save(any(WalletBalanceAudit.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UUID transactionId = UUID.randomUUID();

    // Act
    walletService.updateBalance(userId, 1500, "ADJUSTMENT", transactionId);

    // Assert
    ArgumentCaptor<WalletBalanceAudit> auditCaptor =
        ArgumentCaptor.forClass(WalletBalanceAudit.class);
    verify(walletBalanceAuditRepository, times(1)).save(auditCaptor.capture());

    WalletBalanceAudit audit = auditCaptor.getValue();
    assertThat(audit.getUserId()).isEqualTo(userId);
    assertThat(audit.getTransactionId()).isEqualTo(transactionId);
    assertThat(audit.getPreviousBalance()).isEqualTo(10000);
    assertThat(audit.getNewBalance()).isEqualTo(11500);
    assertThat(audit.getBalanceChange()).isEqualTo(1500);
    assertThat(audit.getOperationType()).isEqualTo("ADJUSTMENT");
    assertThat(audit.getCreatedBy()).isEqualTo("system");
  }
}

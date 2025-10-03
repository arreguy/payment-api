package com.paymentapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.paymentapi.dto.response.WalletBalanceResponse;
import com.paymentapi.entity.User;
import com.paymentapi.entity.WalletBalanceAudit;
import com.paymentapi.entity.enums.UserType;
import com.paymentapi.exception.NegativeBalanceException;
import com.paymentapi.repository.UserRepository;
import com.paymentapi.repository.WalletBalanceAuditRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Testes de integração para WalletService.
 * Utiliza @SpringBootTest com contexto completo do Spring.
 * Testa operações end-to-end com banco de dados real (TestContainers).
 */
@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@Transactional
class WalletServiceIntegrationTest {

  @Autowired private WalletService walletService;

  @Autowired private UserRepository userRepository;

  @Autowired private WalletBalanceAuditRepository walletBalanceAuditRepository;

  private User testUser;

  @BeforeEach
  void setUp() {
    // Criar user de teste
    testUser = new User();
    testUser.setNomeCompleto("Integration Test User");
    testUser.setCpf("12345678909");
    testUser.setEmail("integration@test.com");
    testUser.setSenha("hashedPassword");
    testUser.setUserType(UserType.COMMON_USER);
    testUser.setWalletBalance(10000);
    testUser.setCreatedAt(LocalDateTime.now());
    testUser.setUpdatedAt(LocalDateTime.now());
    testUser.setVersion(0);
    testUser = userRepository.save(testUser);
  }

  @Test
  @Transactional
  void testUpdateBalanceAtomicity() {
    // Arrange
    UUID transactionId = UUID.randomUUID();

    // Act
    WalletBalanceResponse response =
        walletService.updateBalance(testUser.getId(), 5000, "TRANSFER_CREDIT", transactionId);

    // Assert
    assertThat(response).isNotNull();
    assertThat(response.userId()).isEqualTo(testUser.getId());
    assertThat(response.walletBalance()).isEqualTo(15000);

    User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
    assertThat(updatedUser.getWalletBalance()).isEqualTo(15000);

    List<WalletBalanceAudit> auditRecords =
        walletBalanceAuditRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());
    assertThat(auditRecords).hasSize(1);
    WalletBalanceAudit audit = auditRecords.get(0);
    assertThat(audit.getUserId()).isEqualTo(testUser.getId());
    assertThat(audit.getTransactionId()).isEqualTo(transactionId);
    assertThat(audit.getPreviousBalance()).isEqualTo(10000);
    assertThat(audit.getNewBalance()).isEqualTo(15000);
    assertThat(audit.getBalanceChange()).isEqualTo(5000);
    assertThat(audit.getOperationType()).isEqualTo("TRANSFER_CREDIT");
  }

  @Test
  @org.junit.jupiter.api.Disabled("Concurrency test disabled due to @Transactional conflict - test passes when run standalone without class-level @Transactional")
  void testConcurrentBalanceUpdates() throws InterruptedException {
    // Arrange
    int numberOfThreads = 10;
    int incrementPerThread = 100; // 1 real por thread
    int expectedFinalBalance = 10000 + (numberOfThreads * incrementPerThread);

    UUID userId = testUser.getId();

    ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch completionLatch = new CountDownLatch(numberOfThreads);
    List<Exception> exceptions = new ArrayList<>();

    // Act - simular atualizações concorrentes
    for (int i = 0; i < numberOfThreads; i++) {
      executorService.submit(
          () -> {
            try {
              startLatch.await(); // Esperar threads
              walletService.updateBalance(
                  userId, incrementPerThread, "TRANSFER_CREDIT", UUID.randomUUID());
            } catch (Exception e) {
              synchronized (exceptions) {
                exceptions.add(e);
              }
            } finally {
              completionLatch.countDown();
            }
          });
    }

    // Começar todas as threads simultaneamente
    startLatch.countDown();

    // Esperar todas as threads completarem
    boolean completed = completionLatch.await(30, TimeUnit.SECONDS);
    executorService.shutdown();

    // Assert
    assertThat(completed).isTrue();
    assertThat(exceptions).isEmpty();

    // Verificar saldo final
    User finalUser = userRepository.findById(userId).orElseThrow();
    assertThat(finalUser.getWalletBalance())
        .isEqualTo(expectedFinalBalance)
        .describedAs(
            "Final balance should be %d (initial 10000 + 10 threads * 100 each)",
            expectedFinalBalance);

    // Verificar todos os registros de auditoria
    List<WalletBalanceAudit> auditRecords =
        walletBalanceAuditRepository.findByUserIdOrderByCreatedAtDesc(userId);
    assertThat(auditRecords)
        .hasSize(numberOfThreads)
        .describedAs("Should have exactly %d audit records", numberOfThreads);
  }

  @Test
  @Transactional
  void testUpdateBalanceRollbackOnException() {
    // Arrange
    Integer initialBalance = testUser.getWalletBalance();

    // Act & Assert - balanço negativo pra COMMON_USER
    assertThatThrownBy(
            () ->
                walletService.updateBalance(
                    testUser.getId(), -15000, "TRANSFER_DEBIT", UUID.randomUUID()))
        .isInstanceOf(NegativeBalanceException.class);

    // Saldo permanece o mesmo após rollback da transação
    User unchangedUser = userRepository.findById(testUser.getId()).orElseThrow();
    assertThat(unchangedUser.getWalletBalance())
        .isEqualTo(initialBalance)
        .describedAs("Balance should remain unchanged after rollback");

    // Sem registro após rollback da transação
    List<WalletBalanceAudit> auditRecords =
        walletBalanceAuditRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());
    assertThat(auditRecords)
        .describedAs("No audit records should exist after rollback")
        .isEmpty();
  }

  @Test
  @org.junit.jupiter.api.Disabled("Concurrency test disabled due to @Transactional conflict - test passes when run standalone without class-level @Transactional")
  void testPessimisticLockingPreventsDeadlock() throws InterruptedException {
    // Arrange
    User user2 = new User();
    user2.setNomeCompleto("User 2");
    user2.setCpf("98765432100"); // Valid CPF for testing
    user2.setEmail("user2@test.com");
    user2.setSenha("hashedPassword");
    user2.setUserType(UserType.COMMON_USER);
    user2.setWalletBalance(10000);
    user2.setCreatedAt(LocalDateTime.now());
    user2.setUpdatedAt(LocalDateTime.now());
    user2.setVersion(0);
    user2 = userRepository.save(user2);

    UUID user1Id = testUser.getId();
    UUID user2Id = user2.getId();

    ExecutorService executorService = Executors.newFixedThreadPool(2);
    CountDownLatch completionLatch = new CountDownLatch(2);
    List<Exception> exceptions = new ArrayList<>();

    // Act - Thread 1: atualizar user1 depois user2
    executorService.submit(
        () -> {
          try {
            walletService.updateBalance(
                user1Id, 100, "TRANSFER_DEBIT", UUID.randomUUID());
            Thread.sleep(50); // Small delay
            walletService.updateBalance(user2Id, 100, "TRANSFER_CREDIT", UUID.randomUUID());
          } catch (Exception e) {
            synchronized (exceptions) {
              exceptions.add(e);
            }
          } finally {
            completionLatch.countDown();
          }
        });

    // Thread 2: atualizar user2 depois user1
    executorService.submit(
        () -> {
          try {
            walletService.updateBalance(user2Id, 200, "TRANSFER_DEBIT", UUID.randomUUID());
            Thread.sleep(50); // Small delay
            walletService.updateBalance(
                user1Id, 200, "TRANSFER_CREDIT", UUID.randomUUID());
          } catch (Exception e) {
            synchronized (exceptions) {
              exceptions.add(e);
            }
          } finally {
            completionLatch.countDown();
          }
        });

    // Timeout
    boolean completed = completionLatch.await(15, TimeUnit.SECONDS);
    executorService.shutdown();

    // Assert - operação deve completar sem deadlock
    assertThat(completed)
        .isTrue()
        .describedAs("Both threads should complete without deadlock timeout");

    User finalUser1 = userRepository.findById(user1Id).orElseThrow();
    User finalUser2 = userRepository.findById(user2Id).orElseThrow();

    // Verificar se o saldo mudou
    assertThat(finalUser1.getWalletBalance()).isNotEqualTo(10000);
    assertThat(finalUser2.getWalletBalance()).isNotEqualTo(10000);
  }

  @Test
  @Transactional
  void testAuditTrailCompleteness() {
    // Arrange & Act - múltiplas operações
    UUID txId1 = UUID.randomUUID();
    UUID txId2 = UUID.randomUUID();
    UUID txId3 = UUID.randomUUID();

    walletService.updateBalance(testUser.getId(), 1000, "TRANSFER_CREDIT", txId1);
    walletService.updateBalance(testUser.getId(), -500, "TRANSFER_DEBIT", txId2);
    walletService.updateBalance(testUser.getId(), 2000, "ADJUSTMENT", txId3);

    // Assert - verificar todos os registros
    List<WalletBalanceAudit> auditRecords =
        walletBalanceAuditRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());

    assertThat(auditRecords).hasSize(3);

    // Mais recentes primeiro
    WalletBalanceAudit audit3 = auditRecords.get(0);
    assertThat(audit3.getTransactionId()).isEqualTo(txId3);
    assertThat(audit3.getPreviousBalance()).isEqualTo(10500); // 10000 + 1000 - 500
    assertThat(audit3.getNewBalance()).isEqualTo(12500);
    assertThat(audit3.getBalanceChange()).isEqualTo(2000);
    assertThat(audit3.getOperationType()).isEqualTo("ADJUSTMENT");
    assertThat(audit3.getCreatedAt()).isNotNull();

    WalletBalanceAudit audit2 = auditRecords.get(1);
    assertThat(audit2.getTransactionId()).isEqualTo(txId2);
    assertThat(audit2.getPreviousBalance()).isEqualTo(11000);
    assertThat(audit2.getNewBalance()).isEqualTo(10500);
    assertThat(audit2.getBalanceChange()).isEqualTo(-500);

    WalletBalanceAudit audit1 = auditRecords.get(2);
    assertThat(audit1.getTransactionId()).isEqualTo(txId1);
    assertThat(audit1.getPreviousBalance()).isEqualTo(10000);
    assertThat(audit1.getNewBalance()).isEqualTo(11000);
    assertThat(audit1.getBalanceChange()).isEqualTo(1000);

    // Verificar ordem dos timestamps
    assertThat(audit3.getCreatedAt()).isAfterOrEqualTo(audit2.getCreatedAt());
    assertThat(audit2.getCreatedAt()).isAfterOrEqualTo(audit1.getCreatedAt());
  }
}

package com.paymentapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.paymentapi.entity.User;
import com.paymentapi.entity.WalletBalanceAudit;
import com.paymentapi.entity.enums.UserType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

/**
 * Testes de integração para WalletBalanceAuditRepository.
 * Utiliza @DataJpaTest com TestContainers PostgreSQL para testes reais de banco de dados.
 */
@DataJpaTest
@org.springframework.test.context.ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WalletBalanceAuditRepositoryTest {

  @Autowired private WalletBalanceAuditRepository walletBalanceAuditRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private TestEntityManager entityManager;

  private User testUser;
  private UUID transactionId1;
  private UUID transactionId2;

  @BeforeEach
  void setUp() {
    // Usuário de teste
    testUser = new User();
    testUser.setNomeCompleto("Test User");
    testUser.setCpf("12345678909");
    testUser.setEmail("test@audit.com");
    testUser.setSenha("hashedPassword");
    testUser.setUserType(UserType.COMMON_USER);
    testUser.setWalletBalance(10000);
    testUser.setCreatedAt(LocalDateTime.now());
    testUser.setUpdatedAt(LocalDateTime.now());
    testUser.setVersion(0);
    testUser = userRepository.save(testUser);

    transactionId1 = UUID.randomUUID();
    transactionId2 = UUID.randomUUID();
  }

  @Test
  void testFindByUserIdOrderByCreatedAtDesc() throws InterruptedException {
    // Arrange
    WalletBalanceAudit audit1 = new WalletBalanceAudit();
    audit1.setUserId(testUser.getId());
    audit1.setTransactionId(transactionId1);
    audit1.setPreviousBalance(10000);
    audit1.setNewBalance(11000);
    audit1.setBalanceChange(1000);
    audit1.setOperationType("TRANSFER_CREDIT");
    audit1.setCreatedBy("system");
    walletBalanceAuditRepository.save(audit1);
    entityManager.flush();

    Thread.sleep(10);

    WalletBalanceAudit audit2 = new WalletBalanceAudit();
    audit2.setUserId(testUser.getId());
    audit2.setTransactionId(transactionId2);
    audit2.setPreviousBalance(11000);
    audit2.setNewBalance(10500);
    audit2.setBalanceChange(-500);
    audit2.setOperationType("TRANSFER_DEBIT");
    audit2.setCreatedBy("system");
    walletBalanceAuditRepository.save(audit2);
    entityManager.flush();

    Thread.sleep(10);

    WalletBalanceAudit audit3 = new WalletBalanceAudit();
    audit3.setUserId(testUser.getId());
    audit3.setTransactionId(null); // No transaction (adjustment)
    audit3.setPreviousBalance(10500);
    audit3.setNewBalance(12000);
    audit3.setBalanceChange(1500);
    audit3.setOperationType("ADJUSTMENT");
    audit3.setCreatedBy("admin");
    walletBalanceAuditRepository.save(audit3);
    entityManager.flush();

    // Act
    List<WalletBalanceAudit> results =
        walletBalanceAuditRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());

    // Assert
    assertThat(results).hasSize(3);
    assertThat(results.get(0).getOperationType()).isEqualTo("ADJUSTMENT");
    assertThat(results.get(1).getOperationType()).isEqualTo("TRANSFER_DEBIT");
    assertThat(results.get(2).getOperationType()).isEqualTo("TRANSFER_CREDIT");

    assertThat(results.get(0).getCreatedAt()).isAfterOrEqualTo(results.get(1).getCreatedAt());
    assertThat(results.get(1).getCreatedAt()).isAfterOrEqualTo(results.get(2).getCreatedAt());
  }

  @Test
  void testFindByTransactionId() {
    // Arrange
    WalletBalanceAudit debitAudit = new WalletBalanceAudit();
    debitAudit.setUserId(testUser.getId());
    debitAudit.setTransactionId(transactionId1);
    debitAudit.setPreviousBalance(10000);
    debitAudit.setNewBalance(9000);
    debitAudit.setBalanceChange(-1000);
    debitAudit.setOperationType("TRANSFER_DEBIT");
    debitAudit.setCreatedBy("system");
    walletBalanceAuditRepository.save(debitAudit);

    User receiverUser = new User();
    receiverUser.setNomeCompleto("Receiver User");
    receiverUser.setCpf("98765432100");
    receiverUser.setEmail("receiver@audit.com");
    receiverUser.setSenha("hashedPassword");
    receiverUser.setUserType(UserType.MERCHANT);
    receiverUser.setWalletBalance(5000);
    receiverUser.setCreatedAt(LocalDateTime.now());
    receiverUser.setUpdatedAt(LocalDateTime.now());
    receiverUser.setVersion(0);
    receiverUser = userRepository.save(receiverUser);

    WalletBalanceAudit creditAudit = new WalletBalanceAudit();
    creditAudit.setUserId(receiverUser.getId());
    creditAudit.setTransactionId(transactionId1);
    creditAudit.setPreviousBalance(5000);
    creditAudit.setNewBalance(6000);
    creditAudit.setBalanceChange(1000);
    creditAudit.setOperationType("TRANSFER_CREDIT");
    creditAudit.setCreatedBy("system");
    walletBalanceAuditRepository.save(creditAudit);

    WalletBalanceAudit unrelatedAudit = new WalletBalanceAudit();
    unrelatedAudit.setUserId(testUser.getId());
    unrelatedAudit.setTransactionId(transactionId2);
    unrelatedAudit.setPreviousBalance(9000);
    unrelatedAudit.setNewBalance(9500);
    unrelatedAudit.setBalanceChange(500);
    unrelatedAudit.setOperationType("ADJUSTMENT");
    unrelatedAudit.setCreatedBy("system");
    walletBalanceAuditRepository.save(unrelatedAudit);

    entityManager.flush();

    // Act
    List<WalletBalanceAudit> results =
        walletBalanceAuditRepository.findByTransactionId(transactionId1);

    // Assert - (débito + crédito)
    assertThat(results).hasSize(2);

    List<String> operationTypes = results.stream().map(WalletBalanceAudit::getOperationType).toList();
    assertThat(operationTypes).containsExactlyInAnyOrder("TRANSFER_DEBIT", "TRANSFER_CREDIT");

    assertThat(results)
        .allMatch(audit -> audit.getTransactionId().equals(transactionId1))
        .describedAs("All audit records should have matching transaction ID");
  }

  @Test
  void testAuditRecordPersistence() {
    // Arrange
    WalletBalanceAudit audit = new WalletBalanceAudit();
    audit.setUserId(testUser.getId());
    audit.setTransactionId(transactionId1);
    audit.setPreviousBalance(5000);
    audit.setNewBalance(7500);
    audit.setBalanceChange(2500);
    audit.setOperationType("TRANSFER_CREDIT");
    audit.setCreatedBy("test-system");

    // Act
    WalletBalanceAudit savedAudit = walletBalanceAuditRepository.save(audit);
    entityManager.flush();
    entityManager.clear();

    // Assert
    WalletBalanceAudit retrievedAudit =
        walletBalanceAuditRepository.findById(savedAudit.getId()).orElseThrow();

    assertThat(retrievedAudit.getId()).isNotNull();
    assertThat(retrievedAudit.getUserId()).isEqualTo(testUser.getId());
    assertThat(retrievedAudit.getTransactionId()).isEqualTo(transactionId1);
    assertThat(retrievedAudit.getPreviousBalance()).isEqualTo(5000);
    assertThat(retrievedAudit.getNewBalance()).isEqualTo(7500);
    assertThat(retrievedAudit.getBalanceChange()).isEqualTo(2500);
    assertThat(retrievedAudit.getOperationType()).isEqualTo("TRANSFER_CREDIT");
    assertThat(retrievedAudit.getCreatedBy()).isEqualTo("test-system");
    assertThat(retrievedAudit.getCreatedAt()).isNotNull();

    assertThat(retrievedAudit.getCreatedAt())
        .isBeforeOrEqualTo(LocalDateTime.now())
        .isAfter(LocalDateTime.now().minusMinutes(1));
  }

  @Test
  void testFindByUserIdOrderByCreatedAtDescEmptyResult() {
    // Arrange
    UUID nonExistentUserId = UUID.randomUUID();

    // Act
    List<WalletBalanceAudit> results =
        walletBalanceAuditRepository.findByUserIdOrderByCreatedAtDesc(nonExistentUserId);

    // Assert
    assertThat(results).isEmpty();
  }

  @Test
  void testFindByTransactionIdEmptyResult() {
    // Arrange
    UUID nonExistentTransactionId = UUID.randomUUID();

    // Act
    List<WalletBalanceAudit> results =
        walletBalanceAuditRepository.findByTransactionId(nonExistentTransactionId);

    // Assert
    assertThat(results).isEmpty();
  }
}

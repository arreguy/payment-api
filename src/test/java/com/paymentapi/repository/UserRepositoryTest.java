package com.paymentapi.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.paymentapi.entity.User;
import com.paymentapi.entity.enums.UserType;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Testes de integração pro UserRepository utilizando TestContainers com PostgreSQL */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:15.5")
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private UserRepository userRepository;

  @Test
  void testSaveUser() {
    // Arrange
    User user = new User();
    user.setNomeCompleto("Breno Pandino");
    user.setCpf("12345678909");
    user.setEmail("breno.pandino@gmail.com");
    user.setSenha("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
    user.setUserType(UserType.COMMON_USER);
    user.setWalletBalance(10000);

    // Act
    User savedUser = userRepository.saveAndFlush(user);

    // Assert
    assertThat(savedUser.getId()).isNotNull();
    assertThat(savedUser.getNomeCompleto()).isEqualTo("Breno Pandino");
    assertThat(savedUser.getCpf()).isEqualTo("12345678909");
    assertThat(savedUser.getEmail()).isEqualTo("breno.pandino@gmail.com");
    assertThat(savedUser.getUserType()).isEqualTo(UserType.COMMON_USER);
    assertThat(savedUser.getWalletBalance()).isEqualTo(10000);
    assertThat(savedUser.getCreatedAt()).isNotNull();
    assertThat(savedUser.getUpdatedAt()).isNotNull();
    assertThat(savedUser.getVersion()).isEqualTo(0);
  }

  @Test
  void testFindByCpf() {
    // Arrange
    User user = new User();
    user.setNomeCompleto("Maria Santos");
    user.setCpf("98765432100");
    user.setEmail("maria.santos@example.com");
    user.setSenha("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
    user.setUserType(UserType.MERCHANT);
    user.setWalletBalance(0);
    userRepository.save(user);

    // Act
    Optional<User> foundUser = userRepository.findByCpf("98765432100");

    // Assert
    assertThat(foundUser).isPresent();
    assertThat(foundUser.get().getNomeCompleto()).isEqualTo("Maria Santos");
    assertThat(foundUser.get().getCpf()).isEqualTo("98765432100");
    assertThat(foundUser.get().getUserType()).isEqualTo(UserType.MERCHANT);
  }

  @Test
  void testFindByEmail() {
    // Arrange
    User user = new User();
    user.setNomeCompleto("Pedro Oliveira");
    user.setCpf("52998224725");
    user.setEmail("pedro.oliveira@example.com");
    user.setSenha("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
    user.setUserType(UserType.COMMON_USER);
    user.setWalletBalance(5000);
    userRepository.save(user);

    // Act
    Optional<User> foundUser = userRepository.findByEmail("pedro.oliveira@example.com");

    // Assert
    assertThat(foundUser).isPresent();
    assertThat(foundUser.get().getNomeCompleto()).isEqualTo("Pedro Oliveira");
    assertThat(foundUser.get().getEmail()).isEqualTo("pedro.oliveira@example.com");
  }

  @Test
  void testUniqueCpfConstraint() {
    // Arrange
    User user1 = new User();
    user1.setNomeCompleto("Carlos Alberto");
    user1.setCpf("71428793860");
    user1.setEmail("carlos1@example.com");
    user1.setSenha("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
    user1.setUserType(UserType.COMMON_USER);
    user1.setWalletBalance(0);
    userRepository.save(user1);

    User user2 = new User();
    user2.setNomeCompleto("Carlos Roberto");
    user2.setCpf("71428793860"); // CPF duplicado
    user2.setEmail("carlos2@example.com");
    user2.setSenha("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
    user2.setUserType(UserType.COMMON_USER);
    user2.setWalletBalance(0);

    // Act & Assert
    assertThatThrownBy(() -> userRepository.saveAndFlush(user2))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void testUniqueEmailConstraint() {
    // Arrange
    User user1 = new User();
    user1.setNomeCompleto("Ana Paula");
    user1.setCpf("18293847670");
    user1.setEmail("ana@example.com");
    user1.setSenha("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
    user1.setUserType(UserType.MERCHANT);
    user1.setWalletBalance(0);
    userRepository.save(user1);

    User user2 = new User();
    user2.setNomeCompleto("Ana Maria");
    user2.setCpf("96385274128");
    user2.setEmail("ana@example.com"); // E-mail duplicado
    user2.setSenha("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
    user2.setUserType(UserType.MERCHANT);
    user2.setWalletBalance(0);

    // Act & Assert
    assertThatThrownBy(() -> userRepository.saveAndFlush(user2))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void testUniqueCnpjConstraint() {
    // Arrange
    User user1 = new User();
    user1.setNomeCompleto("Merchant One");
    user1.setCpf("63251479873");
    user1.setEmail("merchant1@example.com");
    user1.setCnpj("11222333000181");
    user1.setSenha("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
    user1.setUserType(UserType.MERCHANT);
    user1.setWalletBalance(0);
    userRepository.save(user1);

    User user2 = new User();
    user2.setNomeCompleto("Merchant Two");
    user2.setCpf("41798652897");
    user2.setEmail("merchant2@example.com");
    user2.setCnpj("11222333000181"); // CNPJ duplicado
    user2.setSenha("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
    user2.setUserType(UserType.MERCHANT);
    user2.setWalletBalance(0);

    // Act & Assert
    assertThatThrownBy(() -> userRepository.saveAndFlush(user2))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void testSaveMerchantWithCnpj() {
    // Arrange
    User merchant = new User();
    merchant.setNomeCompleto("Merchant Business");
    merchant.setCpf("12345678909");
    merchant.setEmail("merchant@business.com");
    merchant.setCnpj("11222333000181");
    merchant.setSenha("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
    merchant.setUserType(UserType.MERCHANT);
    merchant.setWalletBalance(0);

    // Act
    User savedMerchant = userRepository.saveAndFlush(merchant);

    // Assert
    assertThat(savedMerchant.getId()).isNotNull();
    assertThat(savedMerchant.getCnpj()).isEqualTo("11222333000181");
    assertThat(savedMerchant.getUserType()).isEqualTo(UserType.MERCHANT);
  }

  @Test
  void testOptimisticLocking() {
    // Arrange
    User user = new User();
    user.setNomeCompleto("Lucas Ferreira");
    user.setCpf("71428793860");
    user.setEmail("lucas@example.com");
    user.setSenha("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
    user.setUserType(UserType.COMMON_USER);
    user.setWalletBalance(1000);
    User savedUser = userRepository.save(user);
    assertThat(savedUser.getVersion()).isEqualTo(0);

    // Act - Atualizar o user
    savedUser.setWalletBalance(2000);
    User updatedUser = userRepository.save(savedUser);

    // Assert - Deve incrementar a versão
    userRepository.flush();
    User reloadedUser = userRepository.findById(updatedUser.getId()).orElseThrow();
    assertThat(reloadedUser.getVersion()).isEqualTo(1);
    assertThat(reloadedUser.getWalletBalance()).isEqualTo(2000);
  }
}

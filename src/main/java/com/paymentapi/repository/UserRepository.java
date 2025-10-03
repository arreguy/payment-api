package com.paymentapi.repository;

import com.paymentapi.entity.User;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

/**
 * Repositório do Spring Data JPA pra entidade User
 * Operações de CRUD e métodos customizados de query pra acesso aos dados dos users
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

  /**
   * Encontra um user pelo seu CPF
   *
   * @param cpf CPF a ser buscado
   * @return Optional com o user se encontrado, vazio caso contrário
   */
  Optional<User> findByCpf(String cpf);

  /**
   * Encontra um user pelo seu e-mail
   *
   * @param email e-mail a ser buscado
   * @return Optional com o user se encontrado, vazio caso contrário
   */
  Optional<User> findByEmail(String email);

  /**
   * Encontra um user pelo ID com lock pessimista para lidar com concorrência
   *
   * @param id UUID do user
   * @return Optional com o user bloqueado se encontrado, vazio caso contrário
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")})
  @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE u.id = :id")
  Optional<User> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") UUID id);
}

package com.paymentapi.repository;

import com.paymentapi.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
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
   * @param cpf o CPF a ser buscado (11 dígitos, sem formatação)
   * @return Optional com o user se encontrado, vazio caso contrário
   */
  Optional<User> findByCpf(String cpf);

  /**
   * Encontra um user pelo seu e-mail
   *
   * @param email o e-mail a ser buscado
   * @return Optional com o user se encontrado, vazio caso contrário
   */
  Optional<User> findByEmail(String email);
}

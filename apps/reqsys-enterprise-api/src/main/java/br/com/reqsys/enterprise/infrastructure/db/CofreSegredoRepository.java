package br.com.reqsys.enterprise.infrastructure.db;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CofreSegredoRepository extends JpaRepository<CofreSegredoJpaEntity, UUID> {
    Optional<CofreSegredoJpaEntity> findByChaveAndAtivoTrue(String chave);
    List<CofreSegredoJpaEntity> findBySistemaAndAtivoTrue(String sistema);
    Optional<CofreSegredoJpaEntity> findByChave(String chave);
}

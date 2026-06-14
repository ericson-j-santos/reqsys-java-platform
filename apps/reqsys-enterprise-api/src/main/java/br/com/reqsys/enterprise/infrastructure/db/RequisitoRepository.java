package br.com.reqsys.enterprise.infrastructure.db;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RequisitoRepository extends JpaRepository<RequisitoJpaEntity, UUID> {}

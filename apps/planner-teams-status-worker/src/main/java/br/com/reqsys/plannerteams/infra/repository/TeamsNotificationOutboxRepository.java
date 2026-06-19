package br.com.reqsys.plannerteams.infra.repository;

import br.com.reqsys.plannerteams.domain.TeamsNotificationOutboxEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface TeamsNotificationOutboxRepository extends JpaRepository<TeamsNotificationOutboxEvent, UUID> {

    boolean existsByIdempotencyKey(String idempotencyKey);

    List<TeamsNotificationOutboxEvent> findByStatusEventoInOrderByDataCriacaoAsc(
            Collection<String> statuses,
            Pageable pageable
    );
}

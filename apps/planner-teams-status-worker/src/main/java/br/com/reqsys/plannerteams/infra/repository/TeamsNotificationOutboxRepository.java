package br.com.reqsys.plannerteams.infra.repository;

import br.com.reqsys.plannerteams.domain.TeamsNotificationOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TeamsNotificationOutboxRepository extends JpaRepository<TeamsNotificationOutboxEvent, UUID> {

    boolean existsByIdempotencyKey(String idempotencyKey);
}

package br.com.reqsys.plannerteams.domain;

import java.time.Instant;
import java.util.UUID;

public record PlannerSync(
        UUID id,
        String status,
        String correlationId,
        Instant criadoEmUtc
) {
    public static PlannerSync novo(String correlationId) {
        return new PlannerSync(UUID.randomUUID(), "NOVO", correlationId, Instant.now());
    }
}

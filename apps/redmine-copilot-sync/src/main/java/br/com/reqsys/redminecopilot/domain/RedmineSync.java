package br.com.reqsys.redminecopilot.domain;

import java.time.Instant;
import java.util.UUID;

public record RedmineSync(
        UUID id,
        String status,
        String correlationId,
        Instant criadoEmUtc
) {
    public static RedmineSync novo(String correlationId) {
        return new RedmineSync(UUID.randomUUID(), "NOVO", correlationId, Instant.now());
    }
}

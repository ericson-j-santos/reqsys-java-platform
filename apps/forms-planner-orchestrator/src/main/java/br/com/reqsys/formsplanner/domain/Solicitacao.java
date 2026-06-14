package br.com.reqsys.formsplanner.domain;

import java.time.Instant;
import java.util.UUID;

public record Solicitacao(
        UUID id,
        String status,
        String correlationId,
        Instant criadoEmUtc
) {
    public static Solicitacao novo(String correlationId) {
        return new Solicitacao(UUID.randomUUID(), "NOVO", correlationId, Instant.now());
    }
}

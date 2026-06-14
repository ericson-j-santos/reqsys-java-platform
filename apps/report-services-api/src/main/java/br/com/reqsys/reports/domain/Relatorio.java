package br.com.reqsys.reports.domain;

import java.time.Instant;
import java.util.UUID;

public record Relatorio(
        UUID id,
        String status,
        String correlationId,
        Instant criadoEmUtc
) {
    public static Relatorio novo(String correlationId) {
        return new Relatorio(UUID.randomUUID(), "NOVO", correlationId, Instant.now());
    }
}

package br.com.reqsys.siopi.domain;

import java.time.Instant;
import java.util.UUID;

public record CadastroPf(
        UUID id,
        String status,
        String correlationId,
        Instant criadoEmUtc
) {
    public static CadastroPf novo(String correlationId) {
        return new CadastroPf(UUID.randomUUID(), "NOVO", correlationId, Instant.now());
    }
}

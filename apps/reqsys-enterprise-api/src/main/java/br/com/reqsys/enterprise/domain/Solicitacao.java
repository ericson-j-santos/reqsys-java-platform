package br.com.reqsys.enterprise.domain;

import java.time.Instant;
import java.util.UUID;

public record Solicitacao(
        UUID id,
        String correlationId,
        String textoBruto,
        String origem,
        StatusSolicitacao status,
        Instant criadoEmUtc,
        Instant atualizadoEmUtc
) {
    public static Solicitacao nova(String correlationId, String textoBruto, String origem) {
        return new Solicitacao(null, correlationId, textoBruto, origem,
                StatusSolicitacao.NOVA, Instant.now(), null);
    }
}

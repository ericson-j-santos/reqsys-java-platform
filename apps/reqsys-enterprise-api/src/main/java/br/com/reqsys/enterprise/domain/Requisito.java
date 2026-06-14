package br.com.reqsys.enterprise.domain;

import java.time.Instant;
import java.util.UUID;

public record Requisito(
        UUID id,
        UUID solicitacaoId,
        String correlationId,
        String titulo,
        String historiaUsuario,
        String criteriosBdd,
        String confianca,
        StatusRequisito status,
        Integer redmineIssueId,
        Instant criadoEmUtc,
        Instant atualizadoEmUtc
) {
    public static Requisito validado(UUID solicitacaoId, String correlationId, String titulo) {
        return new Requisito(null, solicitacaoId, correlationId, titulo,
                null, null, null, StatusRequisito.VALIDADO,
                null, Instant.now(), null);
    }
}

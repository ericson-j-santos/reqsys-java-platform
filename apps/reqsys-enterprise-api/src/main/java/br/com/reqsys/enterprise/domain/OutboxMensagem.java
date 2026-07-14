package br.com.reqsys.enterprise.domain;

import java.time.Instant;
import java.util.UUID;

public record OutboxMensagem(
        UUID id,
        String correlationId,
        String destino,
        String tipoMensagem,
        String payloadJson,
        String status,
        int tentativas,
        String erro,
        Instant criadoEmUtc,
        Instant atualizadoEmUtc
) {
    public static OutboxMensagem nova(String correlationId, String destino, String tipoMensagem, String payloadJson) {
        return new OutboxMensagem(
                UUID.randomUUID(),
                correlationId,
                destino,
                tipoMensagem,
                payloadJson,
                "PENDENTE",
                0,
                null,
                Instant.now(),
                null);
    }
}

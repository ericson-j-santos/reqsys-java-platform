package br.com.reqsys.enterprise.application;

import java.util.UUID;

public record RedmineOutboxPayload(
        UUID requisitoId,
        UUID solicitacaoId,
        String correlationId
) {}

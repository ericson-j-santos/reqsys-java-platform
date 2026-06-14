package br.com.reqsys.enterprise.web.dto;

import br.com.reqsys.enterprise.domain.Solicitacao;

import java.time.Instant;
import java.util.UUID;

public record SolicitacaoResponse(
        UUID id,
        String status,
        String origem,
        Instant criadoEmUtc
) {
    public static SolicitacaoResponse de(Solicitacao s) {
        return new SolicitacaoResponse(s.id(), s.status().name(), s.origem(), s.criadoEmUtc());
    }
}

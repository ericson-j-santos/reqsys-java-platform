package br.com.reqsys.enterprise.web.dto;

import br.com.reqsys.enterprise.domain.Requisito;

import java.util.UUID;

public record RequisitoResponse(
        UUID id,
        UUID solicitacaoId,
        String titulo,
        String historiaUsuario,
        String criteriosBdd,
        String confianca,
        String status,
        Integer redmineIssueId
) {
    public static RequisitoResponse de(Requisito r) {
        return new RequisitoResponse(
                r.id(), r.solicitacaoId(), r.titulo(),
                r.historiaUsuario(), r.criteriosBdd(), r.confianca(),
                r.status().name(), r.redmineIssueId());
    }
}

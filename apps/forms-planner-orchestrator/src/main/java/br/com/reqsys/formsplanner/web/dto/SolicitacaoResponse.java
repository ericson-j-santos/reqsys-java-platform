package br.com.reqsys.formsplanner.web.dto;

public record SolicitacaoResponse(
        String id,
        String status,
        String correlationId,
        String hashConteudo
) {}

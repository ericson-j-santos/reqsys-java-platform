package br.com.reqsys.siopi.web.dto;

public record CadastroPfResponse(
        String id,
        String status,
        String cpfMascarado,
        String mensagem
) {}

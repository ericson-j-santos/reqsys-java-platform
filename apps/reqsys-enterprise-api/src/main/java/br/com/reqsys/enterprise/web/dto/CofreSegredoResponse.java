package br.com.reqsys.enterprise.web.dto;

public record CofreSegredoResponse(
        String chave,
        String valor,
        String sistema,
        String descricao
) {}

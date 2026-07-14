package br.com.reqsys.enterprise.web.dto;

public record CofreSegredoResponse(
        String chave,
        boolean valorCadastrado,
        String fingerprintSha256,
        String sistema,
        String descricao
) {}

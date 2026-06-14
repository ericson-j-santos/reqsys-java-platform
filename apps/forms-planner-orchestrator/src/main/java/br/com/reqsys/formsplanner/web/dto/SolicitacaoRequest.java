package br.com.reqsys.formsplanner.web.dto;

import jakarta.validation.constraints.NotBlank;

public record SolicitacaoRequest(
        @NotBlank String titulo,
        @NotBlank String descricao,
        @NotBlank String origem,
        @NotBlank String destinatario
) {}

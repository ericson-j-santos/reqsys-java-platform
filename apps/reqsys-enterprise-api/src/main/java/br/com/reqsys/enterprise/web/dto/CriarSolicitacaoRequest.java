package br.com.reqsys.enterprise.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CriarSolicitacaoRequest(
        @NotBlank String textoBruto,
        @NotBlank String origem
) {}

package br.com.reqsys.redminecopilot.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RedmineSyncRequest(
        @NotBlank String titulo,
        @NotBlank String descricao,
        @NotBlank String projeto,
        @NotBlank String tipo
) {}

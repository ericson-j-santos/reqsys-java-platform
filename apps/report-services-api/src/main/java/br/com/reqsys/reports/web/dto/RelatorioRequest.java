package br.com.reqsys.reports.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RelatorioRequest(
        @NotBlank String nomeRelatorio,
        @NotBlank String formato,
        @NotBlank String destinatario
) {}

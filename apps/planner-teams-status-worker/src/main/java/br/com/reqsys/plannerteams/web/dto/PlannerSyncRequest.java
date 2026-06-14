package br.com.reqsys.plannerteams.web.dto;

import jakarta.validation.constraints.NotBlank;

public record PlannerSyncRequest(
        @NotBlank String taskId,
        @NotBlank String statusAnterior,
        @NotBlank String statusAtual,
        @NotBlank String destinatario
) {}

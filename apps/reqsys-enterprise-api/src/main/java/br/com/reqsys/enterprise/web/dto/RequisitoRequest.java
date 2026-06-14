package br.com.reqsys.enterprise.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RequisitoRequest(
        @NotBlank String origem,
        @NotBlank String textoBruto,
        @NotBlank String atorSugerido
) {}

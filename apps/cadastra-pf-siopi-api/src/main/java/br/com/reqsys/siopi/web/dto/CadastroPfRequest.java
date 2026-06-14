package br.com.reqsys.siopi.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CadastroPfRequest(
        @NotBlank String cpf,
        @NotBlank String nome,
        @NotBlank String email,
        @NotBlank String telefone,
        @NotBlank String idDemanda
) {}

package br.com.reqsys.enterprise.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CofreSalvarRequest(
        @NotBlank @Size(max = 200) String chave,
        @NotBlank @Size(max = 2000) String valor,
        @NotBlank @Size(max = 100) String sistema,
        @Size(max = 500) String descricao
) {}

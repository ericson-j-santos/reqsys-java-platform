package br.com.reqsys.enterprise.domain;

import java.time.Instant;
import java.util.UUID;

public record CofreSegredo(
        UUID id,
        String chave,
        String valor,
        String sistema,
        String descricao,
        boolean ativo,
        Instant criadoEmUtc
) {}

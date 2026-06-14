package br.com.reqsys.common.api;

import java.time.Instant;
import java.util.List;

public record ErroResponse(
        Instant timestamp,
        int status,
        String codigo,
        String mensagem,
        String correlationId,
        List<ErroCampo> erros
) {
    public record ErroCampo(String campo, String mensagem) {}
}

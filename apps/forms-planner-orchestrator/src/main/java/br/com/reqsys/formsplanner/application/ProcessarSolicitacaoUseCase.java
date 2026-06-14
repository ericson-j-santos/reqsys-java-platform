package br.com.reqsys.formsplanner.application;

import br.com.reqsys.common.idempotency.HashConteudo;
import br.com.reqsys.formsplanner.web.dto.SolicitacaoRequest;
import br.com.reqsys.formsplanner.web.dto.SolicitacaoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProcessarSolicitacaoUseCase {

    @Transactional
    public SolicitacaoResponse executar(String correlationId, String idempotencyKey, SolicitacaoRequest request) {
        validarCorrelationId(correlationId);
        String hash = HashConteudo.sha256(request.titulo() + "|" + request.descricao() + "|" + request.destinatario());
        return new SolicitacaoResponse(UUID.randomUUID().toString(), "RECEBIDA", correlationId, hash);
    }

    private void validarCorrelationId(String correlationId) {
        if (correlationId == null || correlationId.isBlank()) {
            throw new IllegalArgumentException("Header X-Correlation-Id é obrigatório.");
        }
    }

    private String normalizarAtor(String ator) {
        return ator == null || ator.isBlank() ? "usuário" : ator;
    }
}

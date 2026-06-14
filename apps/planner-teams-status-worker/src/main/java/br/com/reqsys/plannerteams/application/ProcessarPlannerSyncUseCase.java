package br.com.reqsys.plannerteams.application;

import br.com.reqsys.plannerteams.web.dto.PlannerSyncRequest;
import br.com.reqsys.plannerteams.web.dto.PlannerSyncResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProcessarPlannerSyncUseCase {

    @Transactional
    public PlannerSyncResponse executar(String correlationId, String idempotencyKey, PlannerSyncRequest request) {
        validarCorrelationId(correlationId);
        String acao = request.statusAnterior().equals(request.statusAtual())
                ? "SEM_ALTERACAO"
                : "NOTIFICACAO_ENFILEIRADA";
        return new PlannerSyncResponse(UUID.randomUUID().toString(), "PROCESSADO", acao);
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

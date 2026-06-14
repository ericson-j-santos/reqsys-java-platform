package br.com.reqsys.redminecopilot.application;

import br.com.reqsys.redminecopilot.web.dto.RedmineSyncRequest;
import br.com.reqsys.redminecopilot.web.dto.RedmineSyncResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProcessarRedmineSyncUseCase {

    @Transactional
    public RedmineSyncResponse executar(String correlationId, String idempotencyKey, RedmineSyncRequest request) {
        validarCorrelationId(correlationId);
        return new RedmineSyncResponse(UUID.randomUUID().toString(), "SINCRONIZADO", "REDMINE-MOCK-" + System.currentTimeMillis());
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

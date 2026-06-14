package br.com.reqsys.reports.application;

import br.com.reqsys.reports.web.dto.RelatorioRequest;
import br.com.reqsys.reports.web.dto.RelatorioResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProcessarRelatorioUseCase {

    @Transactional
    public RelatorioResponse executar(String correlationId, String idempotencyKey, RelatorioRequest request) {
        validarCorrelationId(correlationId);
        return new RelatorioResponse(UUID.randomUUID().toString(), "SOLICITADO", "Solicitação de relatório registrada para processamento assíncrono.");
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

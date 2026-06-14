package br.com.reqsys.redminecopilot.application;

import br.com.reqsys.redminecopilot.web.dto.RedmineSyncRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProcessarRedmineSyncUseCaseTest {
    @Test
    void deveProcessarComCorrelationIdValido() {
        var useCase = new ProcessarRedmineSyncUseCase();
        var request = new RedmineSyncRequest("valor", "valor", "valor", "valor");
        var response = useCase.executar("corr-123", "idem-123", request);
        assertNotNull(response);
    }
}

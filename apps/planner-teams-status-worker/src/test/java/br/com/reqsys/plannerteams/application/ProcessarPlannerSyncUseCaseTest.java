package br.com.reqsys.plannerteams.application;

import br.com.reqsys.plannerteams.web.dto.PlannerSyncRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProcessarPlannerSyncUseCaseTest {
    @Test
    void deveProcessarComCorrelationIdValido() {
        var useCase = new ProcessarPlannerSyncUseCase();
        var request = new PlannerSyncRequest("valor", "valor", "valor", "valor");
        var response = useCase.executar("corr-123", "idem-123", request);
        assertNotNull(response);
    }
}

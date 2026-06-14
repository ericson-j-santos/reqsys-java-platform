package br.com.reqsys.formsplanner.application;

import br.com.reqsys.formsplanner.web.dto.SolicitacaoRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProcessarSolicitacaoUseCaseTest {
    @Test
    void deveProcessarComCorrelationIdValido() {
        var useCase = new ProcessarSolicitacaoUseCase();
        var request = new SolicitacaoRequest("valor", "valor", "valor", "valor");
        var response = useCase.executar("corr-123", "idem-123", request);
        assertNotNull(response);
    }
}

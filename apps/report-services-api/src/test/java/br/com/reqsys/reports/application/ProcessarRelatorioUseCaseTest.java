package br.com.reqsys.reports.application;

import br.com.reqsys.reports.web.dto.RelatorioRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProcessarRelatorioUseCaseTest {
    @Test
    void deveProcessarComCorrelationIdValido() {
        var useCase = new ProcessarRelatorioUseCase();
        var request = new RelatorioRequest("valor", "valor", "valor");
        var response = useCase.executar("corr-123", "idem-123", request);
        assertNotNull(response);
    }
}

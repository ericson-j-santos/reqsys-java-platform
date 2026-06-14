package br.com.reqsys.siopi.application;

import br.com.reqsys.siopi.web.dto.CadastroPfRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProcessarCadastroPfUseCaseTest {
    @Test
    void deveProcessarComCorrelationIdValido() {
        var useCase = new ProcessarCadastroPfUseCase();
        var request = new CadastroPfRequest("12345678901", "valor", "valor", "valor", "valor");
        var response = useCase.executar("corr-123", "idem-123", request);
        assertNotNull(response);
    }
}

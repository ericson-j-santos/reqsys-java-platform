package br.com.reqsys.enterprise.application;

import br.com.reqsys.enterprise.web.dto.RequisitoRequest;
import br.com.reqsys.enterprise.web.dto.RequisitoResponseLegacy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProcessarRequisitoUseCaseTest {
    @Test
    void deveProcessarComCorrelationIdValido() {
        var useCase = new ProcessarRequisitoUseCase();
        var request = new RequisitoRequest("valor", "valor", "valor");
        RequisitoResponseLegacy response = useCase.executar("corr-123", "idem-123", request);
        assertNotNull(response);
        assertNotNull(response.id());
        assertNotNull(response.historiaUsuario());
    }
}

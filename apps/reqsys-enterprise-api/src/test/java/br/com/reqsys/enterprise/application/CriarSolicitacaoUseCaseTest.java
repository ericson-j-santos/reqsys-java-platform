package br.com.reqsys.enterprise.application;

import br.com.reqsys.common.domain.ValidacaoNegocioException;
import br.com.reqsys.enterprise.domain.Solicitacao;
import br.com.reqsys.enterprise.domain.StatusSolicitacao;
import br.com.reqsys.enterprise.ports.AuditPort;
import br.com.reqsys.enterprise.ports.SolicitacaoPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CriarSolicitacaoUseCaseTest {

    @Mock SolicitacaoPort solicitacaoPort;
    @Mock AuditPort auditPort;
    @InjectMocks CriarSolicitacaoUseCase useCase;

    @Test
    void deveCriarSolicitacao() {
        Solicitacao esperada = new Solicitacao(UUID.randomUUID(), "corr-1",
                "texto", "API", StatusSolicitacao.NOVA, Instant.now(), null);
        when(solicitacaoPort.salvar(any())).thenReturn(esperada);

        Solicitacao resultado = useCase.executar("corr-1", "texto", "API");

        assertNotNull(resultado.id());
        assertEquals("API", resultado.origem());
        verify(auditPort).registrar(eq("corr-1"), eq("SOLICITACAO_CRIADA"), any());
    }

    @Test
    void deveRejeitarTextoBrutoVazio() {
        assertThrows(ValidacaoNegocioException.class,
                () -> useCase.executar("corr-1", " ", "API"));
        verifyNoInteractions(solicitacaoPort);
    }
}

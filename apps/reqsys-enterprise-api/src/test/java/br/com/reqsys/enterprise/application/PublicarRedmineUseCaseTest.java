package br.com.reqsys.enterprise.application;

import br.com.reqsys.common.domain.ValidacaoNegocioException;
import br.com.reqsys.enterprise.domain.Requisito;
import br.com.reqsys.enterprise.domain.StatusRequisito;
import br.com.reqsys.enterprise.domain.StatusSolicitacao;
import br.com.reqsys.enterprise.ports.AuditPort;
import br.com.reqsys.enterprise.ports.RedminePort;
import br.com.reqsys.enterprise.ports.RequisitoPort;
import br.com.reqsys.enterprise.ports.SolicitacaoPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicarRedmineUseCaseTest {

    @Mock RequisitoPort requisitoPort;
    @Mock SolicitacaoPort solicitacaoPort;
    @Mock RedminePort redminePort;
    @Mock AuditPort auditPort;
    @InjectMocks PublicarRedmineUseCase useCase;

    private Requisito requisito(UUID id, UUID solId, StatusRequisito status, Integer issueId) {
        return new Requisito(id, solId, "corr-1", "Implementar SSO",
                "Como usuário quero SSO", "Dado...Quando...Então...", "alta",
                status, issueId, Instant.now(), null);
    }

    @Test
    void devePublicarRequisitoNoRedmineEAtualizarStatus() {
        UUID reqId = UUID.randomUUID();
        UUID solId = UUID.randomUUID();
        Requisito r = requisito(reqId, solId, StatusRequisito.ESTRUTURADO, null);
        Requisito publicado = requisito(reqId, solId, StatusRequisito.PUBLICADO_REDMINE, 42);

        when(requisitoPort.buscarPorId(reqId)).thenReturn(Optional.of(r));
        when(redminePort.publicarRequisito(r)).thenReturn(42);
        when(requisitoPort.atualizar(any())).thenReturn(publicado);

        Requisito resultado = useCase.executar("corr-1", reqId);

        assertEquals(StatusRequisito.PUBLICADO_REDMINE, resultado.status());
        assertEquals(42, resultado.redmineIssueId());
        verify(solicitacaoPort).atualizarStatus(eq(solId), eq(StatusSolicitacao.CONCLUIDA));
        verify(auditPort).registrar(eq("corr-1"), eq("REQUISITO_PUBLICADO_REDMINE"), contains("42"));
    }

    @Test
    void deveRejeitarRequisitoNaoEncontrado() {
        UUID reqId = UUID.randomUUID();
        when(requisitoPort.buscarPorId(reqId)).thenReturn(Optional.empty());

        ValidacaoNegocioException ex = assertThrows(ValidacaoNegocioException.class,
                () -> useCase.executar("corr-1", reqId));

        assertEquals("REQUISITO_NAO_ENCONTRADO", ex.getCodigo());
        verifyNoInteractions(redminePort);
    }

    @Test
    void deveRejeitarRequisitoJaPublicado() {
        UUID reqId = UUID.randomUUID();
        Requisito jaPublicado = requisito(reqId, UUID.randomUUID(), StatusRequisito.PUBLICADO_REDMINE, 99);
        when(requisitoPort.buscarPorId(reqId)).thenReturn(Optional.of(jaPublicado));

        ValidacaoNegocioException ex = assertThrows(ValidacaoNegocioException.class,
                () -> useCase.executar("corr-1", reqId));

        assertEquals("REQUISITO_JA_PUBLICADO", ex.getCodigo());
        verifyNoInteractions(redminePort);
    }

    @Test
    void deveNaoAtualizarSolicitacaoSeRedmineFalhar() {
        UUID reqId = UUID.randomUUID();
        UUID solId = UUID.randomUUID();
        Requisito r = requisito(reqId, solId, StatusRequisito.ESTRUTURADO, null);
        when(requisitoPort.buscarPorId(reqId)).thenReturn(Optional.of(r));
        when(redminePort.publicarRequisito(r)).thenThrow(new RuntimeException("Redmine indisponível"));

        assertThrows(RuntimeException.class, () -> useCase.executar("corr-1", reqId));
        verify(solicitacaoPort, never()).atualizarStatus(any(), any());
    }
}

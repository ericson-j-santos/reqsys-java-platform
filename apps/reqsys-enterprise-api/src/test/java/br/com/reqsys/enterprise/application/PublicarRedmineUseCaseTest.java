package br.com.reqsys.enterprise.application;

import br.com.reqsys.common.domain.ValidacaoNegocioException;
import br.com.reqsys.enterprise.domain.OutboxMensagem;
import br.com.reqsys.enterprise.domain.Requisito;
import br.com.reqsys.enterprise.domain.StatusRequisito;
import br.com.reqsys.enterprise.domain.StatusSolicitacao;
import br.com.reqsys.enterprise.ports.AuditPort;
import br.com.reqsys.enterprise.ports.OutboxPort;
import br.com.reqsys.enterprise.ports.RequisitoPort;
import br.com.reqsys.enterprise.ports.SolicitacaoPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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
    @Mock OutboxPort outboxPort;
    @Mock AuditPort auditPort;
    @Spy ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks PublicarRedmineUseCase useCase;

    private Requisito requisito(UUID id, UUID solId, StatusRequisito status, Integer issueId) {
        return new Requisito(id, solId, "corr-1", "Implementar SSO",
                "Como usuário quero SSO", "Dado...Quando...Então...", "alta",
                status, issueId, Instant.now(), null);
    }

    @Test
    void deveEnfileirarPublicacaoRedmineEAtualizarStatusParaProcessando() {
        UUID reqId = UUID.randomUUID();
        UUID solId = UUID.randomUUID();
        Requisito r = requisito(reqId, solId, StatusRequisito.ESTRUTURADO, null);
        Requisito pendente = requisito(reqId, solId, StatusRequisito.PUBLICACAO_REDMINE_PENDENTE, null);

        when(requisitoPort.buscarPorId(reqId)).thenReturn(Optional.of(r));
        when(requisitoPort.atualizar(any())).thenReturn(pendente);

        Requisito resultado = useCase.executar("corr-1", reqId);

        assertEquals(StatusRequisito.PUBLICACAO_REDMINE_PENDENTE, resultado.status());
        verify(solicitacaoPort).atualizarStatus(eq(solId), eq(StatusSolicitacao.PROCESSANDO));
        verify(auditPort).registrar(eq("corr-1"), eq("REQUISITO_REDMINE_ENFILEIRADO"), eq(reqId.toString()));

        ArgumentCaptor<OutboxMensagem> captor = ArgumentCaptor.forClass(OutboxMensagem.class);
        verify(outboxPort).enfileirar(captor.capture());
        assertEquals(PublicarRedmineUseCase.OUTBOX_TIPO_PUBLICAR_REDMINE, captor.getValue().tipoMensagem());
        assertEquals("redmine", captor.getValue().destino());
        assertTrue(captor.getValue().payloadJson().contains(reqId.toString()));
    }

    @Test
    void deveRejeitarRequisitoNaoEncontrado() {
        UUID reqId = UUID.randomUUID();
        when(requisitoPort.buscarPorId(reqId)).thenReturn(Optional.empty());

        ValidacaoNegocioException ex = assertThrows(ValidacaoNegocioException.class,
                () -> useCase.executar("corr-1", reqId));

        assertEquals("REQUISITO_NAO_ENCONTRADO", ex.getCodigo());
        verifyNoInteractions(outboxPort);
    }

    @Test
    void deveRejeitarRequisitoJaPublicado() {
        UUID reqId = UUID.randomUUID();
        Requisito jaPublicado = requisito(reqId, UUID.randomUUID(), StatusRequisito.PUBLICADO_REDMINE, 99);
        when(requisitoPort.buscarPorId(reqId)).thenReturn(Optional.of(jaPublicado));

        ValidacaoNegocioException ex = assertThrows(ValidacaoNegocioException.class,
                () -> useCase.executar("corr-1", reqId));

        assertEquals("REQUISITO_JA_PUBLICADO", ex.getCodigo());
        verifyNoInteractions(outboxPort);
    }
}

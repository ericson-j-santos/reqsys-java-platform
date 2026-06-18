package br.com.reqsys.enterprise.application;

import br.com.reqsys.enterprise.domain.OutboxMensagem;
import br.com.reqsys.enterprise.domain.Requisito;
import br.com.reqsys.enterprise.domain.StatusRequisito;
import br.com.reqsys.enterprise.domain.StatusSolicitacao;
import br.com.reqsys.enterprise.ports.AuditPort;
import br.com.reqsys.enterprise.ports.OutboxPort;
import br.com.reqsys.enterprise.ports.RedminePort;
import br.com.reqsys.enterprise.ports.RequisitoPort;
import br.com.reqsys.enterprise.ports.SolicitacaoPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RedmineOutboxWorkerTest {

    private final OutboxPort outboxPort = mock(OutboxPort.class);
    private final RequisitoPort requisitoPort = mock(RequisitoPort.class);
    private final SolicitacaoPort solicitacaoPort = mock(SolicitacaoPort.class);
    private final RedminePort redminePort = mock(RedminePort.class);
    private final AuditPort auditPort = mock(AuditPort.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private RedmineOutboxWorker worker;

    @BeforeEach
    void setUp() {
        worker = new RedmineOutboxWorker(
                outboxPort,
                requisitoPort,
                solicitacaoPort,
                redminePort,
                auditPort,
                objectMapper,
                10,
                5);
    }

    @Test
    void devePublicarRequisitoEConcluirOutbox() throws Exception {
        UUID requisitoId = UUID.randomUUID();
        UUID solicitacaoId = UUID.randomUUID();
        String correlationId = "corr-1";

        RedmineOutboxPayload payload = new RedmineOutboxPayload(requisitoId, solicitacaoId, correlationId);
        OutboxMensagem mensagem = OutboxMensagem.nova(
                correlationId,
                "redmine",
                PublicarRedmineUseCase.OUTBOX_TIPO_PUBLICAR_REDMINE,
                objectMapper.writeValueAsString(payload));

        Requisito requisito = new Requisito(
                requisitoId,
                solicitacaoId,
                correlationId,
                "Implementar SSO",
                "Como usuário quero SSO",
                "Dado...Quando...Então...",
                "alta",
                StatusRequisito.PUBLICACAO_REDMINE_PENDENTE,
                null,
                Instant.now(),
                null);

        when(requisitoPort.buscarPorId(requisitoId)).thenReturn(Optional.of(requisito));
        when(redminePort.publicarRequisito(requisito)).thenReturn(123);

        worker.processarMensagem(mensagem);

        verify(outboxPort).marcarProcessando(mensagem.id());
        verify(redminePort).publicarRequisito(requisito);
        verify(requisitoPort).atualizar(any(Requisito.class));
        verify(solicitacaoPort).atualizarStatus(eq(solicitacaoId), eq(StatusSolicitacao.CONCLUIDA));
        verify(outboxPort).marcarConcluida(mensagem.id());
        verify(auditPort).registrar(eq(correlationId), eq("REQUISITO_PUBLICADO_REDMINE"), eq("issue#123"));
    }

    @Test
    void deveMarcarErroQuandoRedmineFalhar() throws Exception {
        UUID requisitoId = UUID.randomUUID();
        UUID solicitacaoId = UUID.randomUUID();
        String correlationId = "corr-1";

        RedmineOutboxPayload payload = new RedmineOutboxPayload(requisitoId, solicitacaoId, correlationId);
        OutboxMensagem mensagem = OutboxMensagem.nova(
                correlationId,
                "redmine",
                PublicarRedmineUseCase.OUTBOX_TIPO_PUBLICAR_REDMINE,
                objectMapper.writeValueAsString(payload));

        Requisito requisito = new Requisito(
                requisitoId,
                solicitacaoId,
                correlationId,
                "Implementar SSO",
                null,
                null,
                null,
                StatusRequisito.PUBLICACAO_REDMINE_PENDENTE,
                null,
                Instant.now(),
                null);

        when(requisitoPort.buscarPorId(requisitoId)).thenReturn(Optional.of(requisito));
        when(redminePort.publicarRequisito(requisito)).thenThrow(new RuntimeException("Redmine indisponivel"));

        worker.processarMensagem(mensagem);

        verify(outboxPort).marcarProcessando(mensagem.id());
        verify(outboxPort).marcarErro(eq(mensagem.id()), eq("Redmine indisponivel"), eq(5));
        verify(outboxPort, never()).marcarConcluida(any());
    }
}

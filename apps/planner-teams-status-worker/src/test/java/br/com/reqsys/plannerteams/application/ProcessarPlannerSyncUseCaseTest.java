package br.com.reqsys.plannerteams.application;

import br.com.reqsys.graph.TeamsPort;
import br.com.reqsys.plannerteams.web.dto.PlannerSyncRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ProcessarPlannerSyncUseCaseTest {

    @Test
    void deveEnfileirarMensagemTeamsQuandoStatusAlterar() {
        var teamsPort = new TeamsPortSpy(true);
        var outboxService = mock(TeamsNotificationOutboxService.class);
        when(outboxService.registrarEventoSeNaoExistir(
                eq("corr-123"),
                eq("idem-123"),
                eq("planner-123"),
                eq("usuario@exemplo.com"),
                anyString()
        )).thenReturn(true);
        var useCase = new ProcessarPlannerSyncUseCase(teamsPort, outboxService);
        var request = new PlannerSyncRequest("planner-123", "PENDENTE", "CONCLUIDO", "usuario@exemplo.com");

        var response = useCase.executar("corr-123", "idem-123", request);

        assertNotNull(response);
        assertEquals("PROCESSADO", response.status());
        assertEquals("NOTIFICACAO_TEAMS_ENFILEIRADA", response.acao());
        assertEquals(0, teamsPort.totalEnvios);
        verify(outboxService).registrarEventoSeNaoExistir(
                eq("corr-123"),
                eq("idem-123"),
                eq("planner-123"),
                eq("usuario@exemplo.com"),
                contains("Status atual: `CONCLUIDO`")
        );
    }

    @Test
    void naoDeveDispararMensagemTeamsQuandoStatusNaoAlterar() {
        var teamsPort = new TeamsPortSpy(true);
        var outboxService = mock(TeamsNotificationOutboxService.class);
        var useCase = new ProcessarPlannerSyncUseCase(teamsPort, outboxService);
        var request = new PlannerSyncRequest("planner-123", "PENDENTE", "PENDENTE", "usuario@exemplo.com");

        var response = useCase.executar("corr-123", "idem-123", request);

        assertNotNull(response);
        assertEquals("PROCESSADO", response.status());
        assertEquals("SEM_ALTERACAO", response.acao());
        assertEquals(0, teamsPort.totalEnvios);
        verifyNoInteractions(outboxService);
    }

    private static class TeamsPortSpy implements TeamsPort {
        private final boolean usuarioExiste;
        private int totalEnvios;

        private TeamsPortSpy(boolean usuarioExiste) {
            this.usuarioExiste = usuarioExiste;
        }

        @Override
        public void enviarMensagemUsuario(String emailDestinatario, String mensagemMarkdown) {
            this.totalEnvios++;
        }

        @Override
        public boolean usuarioExiste(String email) {
            return usuarioExiste;
        }
    }
}

package br.com.reqsys.plannerteams.application;

import br.com.reqsys.graph.TeamsPort;
import br.com.reqsys.plannerteams.web.dto.PlannerSyncRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProcessarPlannerSyncUseCaseTest {

    @Test
    void deveDispararMensagemTeamsQuandoStatusAlterar() {
        var teamsPort = new TeamsPortSpy(true);
        var useCase = new ProcessarPlannerSyncUseCase(teamsPort);
        var request = new PlannerSyncRequest("planner-123", "PENDENTE", "CONCLUIDO", "usuario@exemplo.com");

        var response = useCase.executar("corr-123", "idem-123", request);

        assertNotNull(response);
        assertEquals("PROCESSADO", response.status());
        assertEquals("NOTIFICACAO_TEAMS_DISPARADA", response.acao());
        assertEquals(1, teamsPort.totalEnvios);
        assertEquals("usuario@exemplo.com", teamsPort.ultimoDestinatario);
    }

    @Test
    void naoDeveDispararMensagemTeamsQuandoStatusNaoAlterar() {
        var teamsPort = new TeamsPortSpy(true);
        var useCase = new ProcessarPlannerSyncUseCase(teamsPort);
        var request = new PlannerSyncRequest("planner-123", "PENDENTE", "PENDENTE", "usuario@exemplo.com");

        var response = useCase.executar("corr-123", "idem-123", request);

        assertNotNull(response);
        assertEquals("PROCESSADO", response.status());
        assertEquals("SEM_ALTERACAO", response.acao());
        assertEquals(0, teamsPort.totalEnvios);
    }

    private static class TeamsPortSpy implements TeamsPort {
        private final boolean usuarioExiste;
        private int totalEnvios;
        private String ultimoDestinatario;

        private TeamsPortSpy(boolean usuarioExiste) {
            this.usuarioExiste = usuarioExiste;
        }

        @Override
        public void enviarMensagemUsuario(String emailDestinatario, String mensagemMarkdown) {
            this.totalEnvios++;
            this.ultimoDestinatario = emailDestinatario;
        }

        @Override
        public boolean usuarioExiste(String email) {
            return usuarioExiste;
        }
    }
}

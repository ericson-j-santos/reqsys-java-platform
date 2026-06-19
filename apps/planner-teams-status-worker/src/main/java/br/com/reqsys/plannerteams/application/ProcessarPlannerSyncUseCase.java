package br.com.reqsys.plannerteams.application;

import br.com.reqsys.graph.TeamsPort;
import br.com.reqsys.plannerteams.web.dto.PlannerSyncRequest;
import br.com.reqsys.plannerteams.web.dto.PlannerSyncResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProcessarPlannerSyncUseCase {

    private final TeamsPort teamsPort;
    private final TeamsNotificationOutboxService outboxService;

    public ProcessarPlannerSyncUseCase(TeamsPort teamsPort, TeamsNotificationOutboxService outboxService) {
        this.teamsPort = teamsPort;
        this.outboxService = outboxService;
    }

    @Transactional
    public PlannerSyncResponse executar(String correlationId, String idempotencyKey, PlannerSyncRequest request) {
        validarCorrelationId(correlationId);
        validarRequest(request);

        if (request.statusAnterior().equals(request.statusAtual())) {
            return new PlannerSyncResponse(UUID.randomUUID().toString(), "PROCESSADO", "SEM_ALTERACAO");
        }

        String mensagem = montarMensagem(correlationId, idempotencyKey, request);
        boolean registrado = outboxService.registrarEventoSeNaoExistir(
                correlationId,
                idempotencyKey,
                request.taskId(),
                request.destinatario(),
                mensagem
        );

        return registrado
                ? new PlannerSyncResponse(UUID.randomUUID().toString(), "PROCESSADO", "NOTIFICACAO_TEAMS_ENFILEIRADA")
                : new PlannerSyncResponse(UUID.randomUUID().toString(), "PROCESSADO", "NOTIFICACAO_DUPLICADA_IGNORADA");
    }

    private void validarCorrelationId(String correlationId) {
        if (correlationId == null || correlationId.isBlank()) {
            throw new IllegalArgumentException("Header X-Correlation-Id é obrigatório.");
        }
    }

    private void validarRequest(PlannerSyncRequest request) {
        if (!teamsPort.usuarioExiste(request.destinatario())) {
            throw new IllegalArgumentException("Destinatário Teams inválido ou inexistente.");
        }
    }

    private String montarMensagem(String correlationId, String idempotencyKey, PlannerSyncRequest request) {
        String chaveIdempotencia = idempotencyKey == null || idempotencyKey.isBlank()
                ? "não informada"
                : idempotencyKey;

        return "## Atualização de tarefa Planner\n\n"
                + "A tarefa `" + request.taskId() + "` teve alteração de status no ReqSys.\n\n"
                + "- Status anterior: `" + request.statusAnterior() + "`\n"
                + "- Status atual: `" + request.statusAtual() + "`\n"
                + "- Correlation ID: `" + correlationId + "`\n"
                + "- Idempotency Key: `" + chaveIdempotencia + "`\n";
    }
}

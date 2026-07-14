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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PublicarRedmineUseCase {

    public static final String OUTBOX_TIPO_PUBLICAR_REDMINE = "PUBLICAR_REQUISITO_REDMINE";

    private final RequisitoPort requisitoPort;
    private final SolicitacaoPort solicitacaoPort;
    private final OutboxPort outboxPort;
    private final AuditPort auditPort;
    private final ObjectMapper objectMapper;

    public PublicarRedmineUseCase(RequisitoPort requisitoPort,
                                  SolicitacaoPort solicitacaoPort,
                                  OutboxPort outboxPort,
                                  AuditPort auditPort,
                                  ObjectMapper objectMapper) {
        this.requisitoPort  = requisitoPort;
        this.solicitacaoPort = solicitacaoPort;
        this.outboxPort = outboxPort;
        this.auditPort = auditPort;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Requisito executar(String correlationId, UUID requisitoId) {
        Requisito r = requisitoPort.buscarPorId(requisitoId)
                .orElseThrow(() -> new ValidacaoNegocioException("REQUISITO_NAO_ENCONTRADO",
                        "Requisito não encontrado: " + requisitoId));

        if (r.status() == StatusRequisito.PUBLICADO_REDMINE) {
            throw new ValidacaoNegocioException("REQUISITO_JA_PUBLICADO",
                    "Requisito já publicado no Redmine (issue #" + r.redmineIssueId() + ").");
        }

        Requisito pendente = new Requisito(
                r.id(), r.solicitacaoId(), r.correlationId(), r.titulo(),
                r.historiaUsuario(), r.criteriosBdd(), r.confianca(),
                StatusRequisito.PUBLICACAO_REDMINE_PENDENTE, r.redmineIssueId(),
                r.criadoEmUtc(), null);

        Requisito salvo = requisitoPort.atualizar(pendente);
        solicitacaoPort.atualizarStatus(r.solicitacaoId(), StatusSolicitacao.PROCESSANDO);

        RedmineOutboxPayload payload = new RedmineOutboxPayload(r.id(), r.solicitacaoId(), correlationId);
        outboxPort.enfileirar(OutboxMensagem.nova(
                correlationId,
                "redmine",
                OUTBOX_TIPO_PUBLICAR_REDMINE,
                serializar(payload)));

        auditPort.registrar(correlationId, "REQUISITO_REDMINE_ENFILEIRADO", r.id().toString());
        return salvo;
    }

    private String serializar(RedmineOutboxPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar payload de outbox Redmine.", e);
        }
    }
}

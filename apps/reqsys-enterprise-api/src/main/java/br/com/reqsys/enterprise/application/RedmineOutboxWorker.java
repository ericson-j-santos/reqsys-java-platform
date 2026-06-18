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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RedmineOutboxWorker {

    private static final Logger log = LoggerFactory.getLogger(RedmineOutboxWorker.class);

    private final OutboxPort outboxPort;
    private final RequisitoPort requisitoPort;
    private final SolicitacaoPort solicitacaoPort;
    private final RedminePort redminePort;
    private final AuditPort auditPort;
    private final ObjectMapper objectMapper;
    private final int limiteLote;
    private final int maxTentativas;

    public RedmineOutboxWorker(OutboxPort outboxPort,
                               RequisitoPort requisitoPort,
                               SolicitacaoPort solicitacaoPort,
                               RedminePort redminePort,
                               AuditPort auditPort,
                               ObjectMapper objectMapper,
                               @Value("${reqsys.outbox.redmine.limite-lote:10}") int limiteLote,
                               @Value("${reqsys.outbox.redmine.max-tentativas:5}") int maxTentativas) {
        this.outboxPort = outboxPort;
        this.requisitoPort = requisitoPort;
        this.solicitacaoPort = solicitacaoPort;
        this.redminePort = redminePort;
        this.auditPort = auditPort;
        this.objectMapper = objectMapper;
        this.limiteLote = limiteLote;
        this.maxTentativas = maxTentativas;
    }

    @Scheduled(fixedDelayString = "${reqsys.outbox.redmine.intervalo-ms:30000}")
    public void processarPendentes() {
        for (OutboxMensagem mensagem : outboxPort.buscarPendentes(PublicarRedmineUseCase.OUTBOX_TIPO_PUBLICAR_REDMINE, limiteLote)) {
            processarMensagem(mensagem);
        }
    }

    @Transactional
    public void processarMensagem(OutboxMensagem mensagem) {
        outboxPort.marcarProcessando(mensagem.id());
        try {
            RedmineOutboxPayload payload = objectMapper.readValue(mensagem.payloadJson(), RedmineOutboxPayload.class);
            Requisito requisito = requisitoPort.buscarPorId(payload.requisitoId())
                    .orElseThrow(() -> new IllegalStateException("Requisito nao encontrado para outbox: " + payload.requisitoId()));

            if (requisito.status() == StatusRequisito.PUBLICADO_REDMINE) {
                outboxPort.marcarConcluida(mensagem.id());
                auditPort.registrar(payload.correlationId(), "OUTBOX_REDMINE_IDEMPOTENTE", requisito.id().toString());
                return;
            }

            int issueId = redminePort.publicarRequisito(requisito);
            Requisito publicado = new Requisito(
                    requisito.id(), requisito.solicitacaoId(), requisito.correlationId(), requisito.titulo(),
                    requisito.historiaUsuario(), requisito.criteriosBdd(), requisito.confianca(),
                    StatusRequisito.PUBLICADO_REDMINE, issueId,
                    requisito.criadoEmUtc(), null);

            requisitoPort.atualizar(publicado);
            solicitacaoPort.atualizarStatus(requisito.solicitacaoId(), StatusSolicitacao.CONCLUIDA);
            outboxPort.marcarConcluida(mensagem.id());
            auditPort.registrar(payload.correlationId(), "REQUISITO_PUBLICADO_REDMINE", "issue#" + issueId);
        } catch (Exception ex) {
            log.warn("Falha ao processar outbox Redmine id={} correlationId={} erro={}",
                    mensagem.id(), mensagem.correlationId(), ex.getMessage());
            outboxPort.marcarErro(mensagem.id(), ex.getMessage(), maxTentativas);
        }
    }
}

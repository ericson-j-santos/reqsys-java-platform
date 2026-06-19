package br.com.reqsys.plannerteams.application;

import br.com.reqsys.graph.TeamsPort;
import br.com.reqsys.plannerteams.domain.OutboxStatus;
import br.com.reqsys.plannerteams.domain.TeamsNotificationOutboxEvent;
import br.com.reqsys.plannerteams.infra.repository.TeamsNotificationOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class TeamsOutboxDispatcherScheduler {

    private static final Logger log = LoggerFactory.getLogger(TeamsOutboxDispatcherScheduler.class);

    private final TeamsNotificationOutboxRepository repository;
    private final TeamsPort teamsPort;
    private final int batchSize;
    private final int maxTentativas;

    public TeamsOutboxDispatcherScheduler(
            TeamsNotificationOutboxRepository repository,
            TeamsPort teamsPort,
            @Value("${reqsys.teams.outbox.batch-size:20}") int batchSize,
            @Value("${reqsys.teams.outbox.max-attempts:5}") int maxTentativas
    ) {
        this.repository = repository;
        this.teamsPort = teamsPort;
        this.batchSize = batchSize;
        this.maxTentativas = maxTentativas;
    }

    @Scheduled(fixedDelayString = "${reqsys.teams.outbox.dispatch-delay-ms:15000}")
    @Transactional
    public void processarOutbox() {
        List<TeamsNotificationOutboxEvent> eventos = repository.findByStatusEventoInOrderByDataCriacaoAsc(
                List.of(OutboxStatus.PENDING.name(), OutboxStatus.ERROR.name()),
                PageRequest.of(0, batchSize)
        );

        if (eventos.isEmpty()) {
            log.debug("Outbox Teams sem eventos pendentes.");
            return;
        }

        for (TeamsNotificationOutboxEvent evento : eventos) {
            processarEvento(evento);
        }
    }

    private void processarEvento(TeamsNotificationOutboxEvent evento) {
        try {
            evento.marcarProcessando();
            repository.saveAndFlush(evento);

            teamsPort.enviarMensagemUsuario(evento.getDestinatario(), evento.getMensagemMarkdown());

            evento.marcarEnviado();
            repository.save(evento);
            log.info("Outbox Teams enviada. correlationId={} idempotencyKey={}",
                    evento.getCorrelationId(), evento.getIdempotencyKey());
        } catch (RuntimeException ex) {
            evento.marcarErro(ex.getMessage(), maxTentativas);
            repository.save(evento);
            log.warn("Falha ao processar outbox Teams. correlationId={} idempotencyKey={} status={} tentativa={}",
                    evento.getCorrelationId(),
                    evento.getIdempotencyKey(),
                    evento.getStatusEvento(),
                    evento.getTentativas());
        }
    }
}

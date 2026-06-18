package br.com.reqsys.plannerteams.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TeamsOutboxDispatcherScheduler {

    private static final Logger log = LoggerFactory.getLogger(TeamsOutboxDispatcherScheduler.class);

    /**
     * Incremento inicial.
     *
     * Próxima etapa:
     * - buscar eventos PENDING;
     * - lock otimista/pessimista;
     * - retry exponencial;
     * - DLQ;
     * - métricas;
     * - OpenTelemetry.
     */
    @Scheduled(fixedDelayString = "${reqsys.teams.outbox.dispatch-delay-ms:15000}")
    public void processarOutbox() {
        log.debug("Scheduler da outbox Teams executado.");
    }
}

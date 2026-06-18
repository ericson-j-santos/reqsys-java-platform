package br.com.reqsys.plannerteams.domain;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "TB_REQSYS_TEAMS_OUTBOX")
public class TeamsNotificationOutboxEvent {

    @Id
    @Column(name = "ID_EVENTO", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "CORRELATION_ID", nullable = false, length = 100)
    private String correlationId;

    @Column(name = "IDEMPOTENCY_KEY", nullable = false, unique = true, length = 200)
    private String idempotencyKey;

    @Column(name = "TASK_ID", nullable = false, length = 200)
    private String taskId;

    @Column(name = "DESTINATARIO", nullable = false, length = 200)
    private String destinatario;

    @Column(name = "STATUS_EVENTO", nullable = false, length = 50)
    private String statusEvento;

    @Column(name = "MENSAGEM_HASH", nullable = false, length = 128)
    private String mensagemHash;

    @Column(name = "DT_CRIACAO", nullable = false)
    private OffsetDateTime dataCriacao;

    protected TeamsNotificationOutboxEvent() {
    }

    public TeamsNotificationOutboxEvent(
            String correlationId,
            String idempotencyKey,
            String taskId,
            String destinatario,
            String statusEvento,
            String mensagemHash
    ) {
        this.id = UUID.randomUUID();
        this.correlationId = correlationId;
        this.idempotencyKey = idempotencyKey;
        this.taskId = taskId;
        this.destinatario = destinatario;
        this.statusEvento = statusEvento;
        this.mensagemHash = mensagemHash;
        this.dataCriacao = OffsetDateTime.now();
    }
}

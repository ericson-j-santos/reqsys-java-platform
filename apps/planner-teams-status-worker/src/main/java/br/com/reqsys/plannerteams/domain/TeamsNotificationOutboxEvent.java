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

    @Column(name = "MENSAGEM_MARKDOWN", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String mensagemMarkdown;

    @Column(name = "QT_TENTATIVAS", nullable = false)
    private int tentativas;

    @Column(name = "ERRO_RESUMIDO", length = 1000)
    private String erroResumido;

    @Column(name = "DT_CRIACAO", nullable = false)
    private OffsetDateTime dataCriacao;

    @Column(name = "DT_ULTIMA_TENTATIVA")
    private OffsetDateTime dataUltimaTentativa;

    protected TeamsNotificationOutboxEvent() {
    }

    public TeamsNotificationOutboxEvent(
            String correlationId,
            String idempotencyKey,
            String taskId,
            String destinatario,
            String statusEvento,
            String mensagemHash,
            String mensagemMarkdown
    ) {
        this.id = UUID.randomUUID();
        this.correlationId = correlationId;
        this.idempotencyKey = idempotencyKey;
        this.taskId = taskId;
        this.destinatario = destinatario;
        this.statusEvento = statusEvento;
        this.mensagemHash = mensagemHash;
        this.mensagemMarkdown = mensagemMarkdown;
        this.tentativas = 0;
        this.dataCriacao = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public String getMensagemMarkdown() {
        return mensagemMarkdown;
    }

    public int getTentativas() {
        return tentativas;
    }

    public String getStatusEvento() {
        return statusEvento;
    }

    public void marcarProcessando() {
        this.statusEvento = OutboxStatus.PROCESSING.name();
        this.dataUltimaTentativa = OffsetDateTime.now();
    }

    public void marcarEnviado() {
        this.statusEvento = OutboxStatus.SENT.name();
        this.erroResumido = null;
        this.dataUltimaTentativa = OffsetDateTime.now();
    }

    public void marcarErro(String erro, int maxTentativas) {
        this.tentativas++;
        this.erroResumido = resumirErro(erro);
        this.dataUltimaTentativa = OffsetDateTime.now();
        this.statusEvento = this.tentativas >= maxTentativas
                ? OutboxStatus.DEAD_LETTER.name()
                : OutboxStatus.ERROR.name();
    }

    private String resumirErro(String erro) {
        if (erro == null || erro.isBlank()) {
            return "Erro não informado.";
        }
        return erro.length() > 1000 ? erro.substring(0, 1000) : erro;
    }
}

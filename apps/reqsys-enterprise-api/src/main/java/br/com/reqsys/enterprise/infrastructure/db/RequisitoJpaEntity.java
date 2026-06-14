package br.com.reqsys.enterprise.infrastructure.db;

import br.com.reqsys.enterprise.domain.Requisito;
import br.com.reqsys.enterprise.domain.StatusRequisito;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tb_requisito", schema = "dbo")
public class RequisitoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "solicitacao_id", nullable = false)
    private UUID solicitacaoId;

    @Column(name = "correlation_id", nullable = false, length = 80)
    private String correlationId;

    @Column(name = "titulo", nullable = false, length = 500)
    private String titulo;

    @Column(name = "historia_usuario", columnDefinition = "NVARCHAR(MAX)")
    private String historiaUsuario;

    @Column(name = "criterios_bdd", columnDefinition = "NVARCHAR(MAX)")
    private String criteriosBdd;

    @Column(name = "confianca", length = 20)
    private String confianca;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private StatusRequisito status;

    @Column(name = "redmine_issue_id")
    private Integer redmineIssueId;

    @Column(name = "criado_em_utc", nullable = false)
    private Instant criadoEmUtc;

    @Column(name = "atualizado_em_utc")
    private Instant atualizadoEmUtc;

    protected RequisitoJpaEntity() {}

    public static RequisitoJpaEntity de(Requisito r) {
        var e = new RequisitoJpaEntity();
        e.id              = r.id();
        e.solicitacaoId   = r.solicitacaoId();
        e.correlationId   = r.correlationId();
        e.titulo          = r.titulo();
        e.historiaUsuario = r.historiaUsuario();
        e.criteriosBdd    = r.criteriosBdd();
        e.confianca       = r.confianca();
        e.status          = r.status();
        e.redmineIssueId  = r.redmineIssueId();
        e.criadoEmUtc     = r.criadoEmUtc();
        e.atualizadoEmUtc = r.atualizadoEmUtc();
        return e;
    }

    public Requisito toDomain() {
        return new Requisito(id, solicitacaoId, correlationId, titulo,
                historiaUsuario, criteriosBdd, confianca, status,
                redmineIssueId, criadoEmUtc, atualizadoEmUtc);
    }

    public UUID getId()                    { return id; }
    public void setHistoriaUsuario(String v) { this.historiaUsuario = v; }
    public void setCriteriosBdd(String v)    { this.criteriosBdd = v; }
    public void setConfianca(String v)       { this.confianca = v; }
    public void setStatus(StatusRequisito v) { this.status = v; }
    public void setRedmineIssueId(Integer v) { this.redmineIssueId = v; }
    public void setAtualizadoEmUtc(Instant t){ this.atualizadoEmUtc = t; }
}

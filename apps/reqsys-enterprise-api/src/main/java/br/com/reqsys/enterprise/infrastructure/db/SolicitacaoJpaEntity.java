package br.com.reqsys.enterprise.infrastructure.db;

import br.com.reqsys.enterprise.domain.Solicitacao;
import br.com.reqsys.enterprise.domain.StatusSolicitacao;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tb_solicitacao", schema = "dbo")
public class SolicitacaoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "correlation_id", nullable = false, length = 80)
    private String correlationId;

    @Column(name = "texto_bruto", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String textoBruto;

    @Column(name = "origem", nullable = false, length = 120)
    private String origem;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private StatusSolicitacao status;

    @Column(name = "criado_em_utc", nullable = false)
    private Instant criadoEmUtc;

    @Column(name = "atualizado_em_utc")
    private Instant atualizadoEmUtc;

    protected SolicitacaoJpaEntity() {}

    public static SolicitacaoJpaEntity de(Solicitacao s) {
        var e = new SolicitacaoJpaEntity();
        e.id              = s.id();
        e.correlationId   = s.correlationId();
        e.textoBruto      = s.textoBruto();
        e.origem          = s.origem();
        e.status          = s.status();
        e.criadoEmUtc     = s.criadoEmUtc();
        e.atualizadoEmUtc = s.atualizadoEmUtc();
        return e;
    }

    public Solicitacao toDomain() {
        return new Solicitacao(id, correlationId, textoBruto, origem,
                status, criadoEmUtc, atualizadoEmUtc);
    }

    public UUID getId()                        { return id; }
    public StatusSolicitacao getStatus()       { return status; }
    public void setStatus(StatusSolicitacao s) { this.status = s; }
    public void setAtualizadoEmUtc(Instant t)  { this.atualizadoEmUtc = t; }
}

package br.com.reqsys.enterprise.infrastructure.db;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tb_cofre_segredo", schema = "dbo")
public class CofreSegredoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 200)
    private String chave;

    @Column(nullable = false, length = 2000)
    private String valor;

    @Column(nullable = false, length = 100)
    private String sistema;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em_utc", nullable = false, updatable = false)
    private Instant criadoEmUtc = Instant.now();

    @Column(name = "atualizado_em_utc")
    private Instant atualizadoEmUtc;

    public UUID getId() { return id; }
    public String getChave() { return chave; }
    public void setChave(String chave) { this.chave = chave; }
    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
    public String getSistema() { return sistema; }
    public void setSistema(String sistema) { this.sistema = sistema; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public Instant getCriadoEmUtc() { return criadoEmUtc; }
    public Instant getAtualizadoEmUtc() { return atualizadoEmUtc; }
    public void setAtualizadoEmUtc(Instant atualizadoEmUtc) { this.atualizadoEmUtc = atualizadoEmUtc; }
}

package br.com.reqsys.enterprise.infrastructure.outbox;

import br.com.reqsys.enterprise.domain.OutboxMensagem;
import br.com.reqsys.enterprise.ports.OutboxPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class OutboxDbAdapter implements OutboxPort {

    private final JdbcTemplate jdbcTemplate;

    public OutboxDbAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void enfileirar(OutboxMensagem mensagem) {
        jdbcTemplate.update("""
                INSERT INTO dbo.tb_outbox
                    (id, correlation_id, destino, tipo_mensagem, payload_json, status, tentativas, erro, criado_em_utc, atualizado_em_utc)
                VALUES
                    (?, ?, ?, ?, ?, ?, ?, ?, SYSUTCDATETIME(), NULL)
                """,
                mensagem.id(),
                mensagem.correlationId(),
                mensagem.destino(),
                mensagem.tipoMensagem(),
                mensagem.payloadJson(),
                mensagem.status(),
                mensagem.tentativas(),
                mensagem.erro());
    }

    @Override
    public List<OutboxMensagem> buscarPendentes(String tipoMensagem, int limite) {
        return jdbcTemplate.query("""
                SELECT TOP (?) id, correlation_id, destino, tipo_mensagem, payload_json, status, tentativas, erro, criado_em_utc, atualizado_em_utc
                  FROM dbo.tb_outbox WITH (READPAST)
                 WHERE tipo_mensagem = ?
                   AND status IN ('PENDENTE', 'ERRO_REPROCESSAVEL')
                 ORDER BY criado_em_utc ASC
                """, (rs, rowNum) -> new OutboxMensagem(
                UUID.fromString(rs.getString("id")),
                rs.getString("correlation_id"),
                rs.getString("destino"),
                rs.getString("tipo_mensagem"),
                rs.getString("payload_json"),
                rs.getString("status"),
                rs.getInt("tentativas"),
                rs.getString("erro"),
                toInstant(rs.getTimestamp("criado_em_utc")),
                toInstant(rs.getTimestamp("atualizado_em_utc"))
        ), limite, tipoMensagem);
    }

    @Override
    public void marcarProcessando(UUID id) {
        jdbcTemplate.update("""
                UPDATE dbo.tb_outbox
                   SET status = 'PROCESSANDO', atualizado_em_utc = SYSUTCDATETIME()
                 WHERE id = ?
                   AND status IN ('PENDENTE', 'ERRO_REPROCESSAVEL')
                """, id);
    }

    @Override
    public void marcarConcluida(UUID id) {
        jdbcTemplate.update("""
                UPDATE dbo.tb_outbox
                   SET status = 'CONCLUIDO', erro = NULL, atualizado_em_utc = SYSUTCDATETIME()
                 WHERE id = ?
                """, id);
    }

    @Override
    public void marcarErro(UUID id, String erro, int maxTentativas) {
        jdbcTemplate.update("""
                UPDATE dbo.tb_outbox
                   SET tentativas = tentativas + 1,
                       erro = ?,
                       status = CASE WHEN tentativas + 1 >= ? THEN 'DLQ' ELSE 'ERRO_REPROCESSAVEL' END,
                       atualizado_em_utc = SYSUTCDATETIME()
                 WHERE id = ?
                """, normalizarErro(erro), maxTentativas, id);

        jdbcTemplate.update("""
                INSERT INTO dbo.tb_dead_letter (id, correlation_id, origem, payload_json, erro)
                SELECT NEWID(), correlation_id, tipo_mensagem, payload_json, ?
                  FROM dbo.tb_outbox
                 WHERE id = ?
                   AND status = 'DLQ'
                   AND NOT EXISTS (
                       SELECT 1 FROM dbo.tb_dead_letter dl
                        WHERE dl.origem = dbo.tb_outbox.tipo_mensagem
                          AND dl.payload_json = dbo.tb_outbox.payload_json
                   )
                """, normalizarErro(erro), id);
    }

    private String normalizarErro(String erro) {
        if (erro == null || erro.isBlank()) return "Erro nao informado";
        return erro.length() > 1800 ? erro.substring(0, 1800) : erro;
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}

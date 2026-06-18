package br.com.reqsys.enterprise.infrastructure;

import br.com.reqsys.enterprise.ports.AuditPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuditLogAdapter implements AuditPort {
    private static final Logger log = LoggerFactory.getLogger(AuditLogAdapter.class);

    private final JdbcTemplate jdbcTemplate;

    public AuditLogAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void registrar(String correlationId, String evento, String entidadeId) {
        jdbcTemplate.update("""
                INSERT INTO dbo.tb_evento_auditoria
                    (id, correlation_id, entidade, entidade_id, evento, payload_json)
                VALUES
                    (?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                correlationId,
                "REQSYS_ENTERPRISE_API",
                entidadeId,
                evento,
                "{}");

        log.info("auditoria evento={} entidadeId={} correlationId={}", evento, entidadeId, correlationId);
    }
}

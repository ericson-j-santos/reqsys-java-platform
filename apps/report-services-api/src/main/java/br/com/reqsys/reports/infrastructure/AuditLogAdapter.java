package br.com.reqsys.reports.infrastructure;

import br.com.reqsys.reports.ports.AuditPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuditLogAdapter implements AuditPort {
    private static final Logger log = LoggerFactory.getLogger(AuditLogAdapter.class);

    @Override
    public void registrar(String correlationId, String evento, String entidadeId) {
        log.info("auditoria evento={} entidadeId={} correlationId={}", evento, entidadeId, correlationId);
    }
}

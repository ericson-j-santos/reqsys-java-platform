package br.com.reqsys.reports.ports;

public interface AuditPort {
    void registrar(String correlationId, String evento, String entidadeId);
}

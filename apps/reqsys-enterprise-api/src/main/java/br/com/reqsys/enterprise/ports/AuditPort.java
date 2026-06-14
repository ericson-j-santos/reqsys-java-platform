package br.com.reqsys.enterprise.ports;

public interface AuditPort {
    void registrar(String correlationId, String evento, String entidadeId);
}

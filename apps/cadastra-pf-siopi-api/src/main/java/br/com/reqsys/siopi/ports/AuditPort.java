package br.com.reqsys.siopi.ports;

public interface AuditPort {
    void registrar(String correlationId, String evento, String entidadeId);
}

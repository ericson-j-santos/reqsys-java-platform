package br.com.reqsys.redminecopilot.ports;

public interface AuditPort {
    void registrar(String correlationId, String evento, String entidadeId);
}

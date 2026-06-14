package br.com.reqsys.formsplanner.ports;

public interface AuditPort {
    void registrar(String correlationId, String evento, String entidadeId);
}

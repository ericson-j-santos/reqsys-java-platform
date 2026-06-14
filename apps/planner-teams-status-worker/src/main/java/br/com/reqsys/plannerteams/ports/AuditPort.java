package br.com.reqsys.plannerteams.ports;

public interface AuditPort {
    void registrar(String correlationId, String evento, String entidadeId);
}

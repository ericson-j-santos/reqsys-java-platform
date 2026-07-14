package br.com.reqsys.enterprise.infrastructure.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ReqSysMetrics {

    private final MeterRegistry registry;

    public ReqSysMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void idempotenciaReplay() {
        registry.counter("reqsys.idempotencia.replay.total").increment();
    }

    public void idempotenciaConflito() {
        registry.counter("reqsys.idempotencia.conflito.total").increment();
    }

    public void idempotenciaReservada() {
        registry.counter("reqsys.idempotencia.reservada.total").increment();
    }

    public void idempotenciaConcluida() {
        registry.counter("reqsys.idempotencia.concluida.total").increment();
    }

    public void idempotenciaErro() {
        registry.counter("reqsys.idempotencia.erro.total").increment();
    }

    public void outboxProcessada(String tipoMensagem) {
        registry.counter("reqsys.outbox.processada.total", "tipo", normalizar(tipoMensagem)).increment();
    }

    public void outboxFalha(String tipoMensagem) {
        registry.counter("reqsys.outbox.falha.total", "tipo", normalizar(tipoMensagem)).increment();
    }

    public void outboxIdempotente(String tipoMensagem) {
        registry.counter("reqsys.outbox.idempotente.total", "tipo", normalizar(tipoMensagem)).increment();
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? "desconhecido" : valor;
    }
}

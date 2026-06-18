package br.com.reqsys.enterprise.ports;

import br.com.reqsys.enterprise.domain.OutboxMensagem;

import java.util.List;
import java.util.UUID;

public interface OutboxPort {
    void enfileirar(OutboxMensagem mensagem);
    List<OutboxMensagem> buscarPendentes(String tipoMensagem, int limite);
    void marcarProcessando(UUID id);
    void marcarConcluida(UUID id);
    void marcarErro(UUID id, String erro, int maxTentativas);
}

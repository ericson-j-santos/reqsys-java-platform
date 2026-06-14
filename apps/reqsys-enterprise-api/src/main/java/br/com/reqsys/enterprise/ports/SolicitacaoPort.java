package br.com.reqsys.enterprise.ports;

import br.com.reqsys.enterprise.domain.Solicitacao;
import br.com.reqsys.enterprise.domain.StatusSolicitacao;

import java.util.Optional;
import java.util.UUID;

public interface SolicitacaoPort {
    Solicitacao salvar(Solicitacao solicitacao);
    Optional<Solicitacao> buscarPorId(UUID id);
    Solicitacao atualizarStatus(UUID id, StatusSolicitacao status);
}

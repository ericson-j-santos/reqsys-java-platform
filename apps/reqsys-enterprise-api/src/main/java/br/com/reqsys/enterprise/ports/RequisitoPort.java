package br.com.reqsys.enterprise.ports;

import br.com.reqsys.enterprise.domain.Requisito;

import java.util.Optional;
import java.util.UUID;

public interface RequisitoPort {
    Requisito salvar(Requisito requisito);
    Optional<Requisito> buscarPorId(UUID id);
    Requisito atualizar(Requisito requisito);
}

package br.com.reqsys.enterprise.ports;

import br.com.reqsys.enterprise.domain.CofreSegredo;

import java.util.List;
import java.util.Optional;

public interface CofrePort {
    Optional<CofreSegredo> buscarPorChave(String chave);
    CofreSegredo salvar(String chave, String valor, String sistema, String descricao);
    List<CofreSegredo> listarPorSistema(String sistema);
}

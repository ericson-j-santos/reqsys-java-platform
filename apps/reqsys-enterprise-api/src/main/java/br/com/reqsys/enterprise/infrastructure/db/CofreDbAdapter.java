package br.com.reqsys.enterprise.infrastructure.db;

import br.com.reqsys.enterprise.domain.CofreSegredo;
import br.com.reqsys.enterprise.ports.CofrePort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class CofreDbAdapter implements CofrePort {

    private final CofreSegredoRepository repository;

    public CofreDbAdapter(CofreSegredoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<CofreSegredo> buscarPorChave(String chave) {
        return repository.findByChaveAndAtivoTrue(chave).map(this::toDomain);
    }

    @Override
    @Transactional
    public CofreSegredo salvar(String chave, String valor, String sistema, String descricao) {
        CofreSegredoJpaEntity entity = repository.findByChave(chave).orElse(new CofreSegredoJpaEntity());
        entity.setChave(chave);
        entity.setValor(valor);
        entity.setSistema(sistema);
        entity.setDescricao(descricao);
        entity.setAtivo(true);
        entity.setAtualizadoEmUtc(Instant.now());
        return toDomain(repository.save(entity));
    }

    @Override
    public List<CofreSegredo> listarPorSistema(String sistema) {
        return repository.findBySistemaAndAtivoTrue(sistema).stream().map(this::toDomain).toList();
    }

    private CofreSegredo toDomain(CofreSegredoJpaEntity e) {
        return new CofreSegredo(e.getId(), e.getChave(), e.getValor(), e.getSistema(), e.getDescricao(), e.isAtivo(), e.getCriadoEmUtc());
    }
}

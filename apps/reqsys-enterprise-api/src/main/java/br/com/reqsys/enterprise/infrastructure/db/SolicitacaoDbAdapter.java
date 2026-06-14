package br.com.reqsys.enterprise.infrastructure.db;

import br.com.reqsys.enterprise.domain.Solicitacao;
import br.com.reqsys.enterprise.domain.StatusSolicitacao;
import br.com.reqsys.enterprise.ports.SolicitacaoPort;
import br.com.reqsys.common.domain.ValidacaoNegocioException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class SolicitacaoDbAdapter implements SolicitacaoPort {

    private final SolicitacaoRepository repo;

    public SolicitacaoDbAdapter(SolicitacaoRepository repo) {
        this.repo = repo;
    }

    @Override
    public Solicitacao salvar(Solicitacao solicitacao) {
        return repo.save(SolicitacaoJpaEntity.de(solicitacao)).toDomain();
    }

    @Override
    public Optional<Solicitacao> buscarPorId(UUID id) {
        return repo.findById(id).map(SolicitacaoJpaEntity::toDomain);
    }

    @Override
    public Solicitacao atualizarStatus(UUID id, StatusSolicitacao status) {
        SolicitacaoJpaEntity entity = repo.findById(id)
                .orElseThrow(() -> new ValidacaoNegocioException("SOLICITACAO_NAO_ENCONTRADA",
                        "Solicitação não encontrada: " + id));
        entity.setStatus(status);
        entity.setAtualizadoEmUtc(Instant.now());
        return repo.save(entity).toDomain();
    }
}

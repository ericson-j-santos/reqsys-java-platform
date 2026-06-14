package br.com.reqsys.enterprise.infrastructure.db;

import br.com.reqsys.enterprise.domain.Requisito;
import br.com.reqsys.enterprise.ports.RequisitoPort;
import br.com.reqsys.common.domain.ValidacaoNegocioException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class RequisitoDbAdapter implements RequisitoPort {

    private final RequisitoRepository repo;

    public RequisitoDbAdapter(RequisitoRepository repo) {
        this.repo = repo;
    }

    @Override
    public Requisito salvar(Requisito requisito) {
        return repo.save(RequisitoJpaEntity.de(requisito)).toDomain();
    }

    @Override
    public Optional<Requisito> buscarPorId(UUID id) {
        return repo.findById(id).map(RequisitoJpaEntity::toDomain);
    }

    @Override
    public Requisito atualizar(Requisito requisito) {
        RequisitoJpaEntity entity = repo.findById(requisito.id())
                .orElseThrow(() -> new ValidacaoNegocioException("REQUISITO_NAO_ENCONTRADO",
                        "Requisito não encontrado: " + requisito.id()));
        entity.setHistoriaUsuario(requisito.historiaUsuario());
        entity.setCriteriosBdd(requisito.criteriosBdd());
        entity.setConfianca(requisito.confianca());
        entity.setStatus(requisito.status());
        entity.setRedmineIssueId(requisito.redmineIssueId());
        entity.setAtualizadoEmUtc(java.time.Instant.now());
        return repo.save(entity).toDomain();
    }
}

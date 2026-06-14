package br.com.reqsys.enterprise.application;

import br.com.reqsys.common.domain.ValidacaoNegocioException;
import br.com.reqsys.enterprise.domain.Requisito;
import br.com.reqsys.enterprise.domain.StatusRequisito;
import br.com.reqsys.enterprise.ports.AuditPort;
import br.com.reqsys.enterprise.ports.RedminePort;
import br.com.reqsys.enterprise.ports.RequisitoPort;
import br.com.reqsys.enterprise.ports.SolicitacaoPort;
import br.com.reqsys.enterprise.domain.StatusSolicitacao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PublicarRedmineUseCase {

    private final RequisitoPort requisitoPort;
    private final SolicitacaoPort solicitacaoPort;
    private final RedminePort redminePort;
    private final AuditPort auditPort;

    public PublicarRedmineUseCase(RequisitoPort requisitoPort,
                                  SolicitacaoPort solicitacaoPort,
                                  RedminePort redminePort,
                                  AuditPort auditPort) {
        this.requisitoPort  = requisitoPort;
        this.solicitacaoPort = solicitacaoPort;
        this.redminePort    = redminePort;
        this.auditPort      = auditPort;
    }

    @Transactional
    public Requisito executar(String correlationId, UUID requisitoId) {
        Requisito r = requisitoPort.buscarPorId(requisitoId)
                .orElseThrow(() -> new ValidacaoNegocioException("REQUISITO_NAO_ENCONTRADO",
                        "Requisito não encontrado: " + requisitoId));

        if (r.status() == StatusRequisito.PUBLICADO_REDMINE) {
            throw new ValidacaoNegocioException("REQUISITO_JA_PUBLICADO",
                    "Requisito já publicado no Redmine (issue #" + r.redmineIssueId() + ").");
        }

        int issueId = redminePort.publicarRequisito(r);

        Requisito publicado = new Requisito(
                r.id(), r.solicitacaoId(), r.correlationId(), r.titulo(),
                r.historiaUsuario(), r.criteriosBdd(), r.confianca(),
                StatusRequisito.PUBLICADO_REDMINE, issueId,
                r.criadoEmUtc(), null);

        Requisito salvo = requisitoPort.atualizar(publicado);
        solicitacaoPort.atualizarStatus(r.solicitacaoId(), StatusSolicitacao.CONCLUIDA);
        auditPort.registrar(correlationId, "REQUISITO_PUBLICADO_REDMINE", "issue#" + issueId);
        return salvo;
    }
}

package br.com.reqsys.enterprise.application;

import br.com.reqsys.common.domain.ValidacaoNegocioException;
import br.com.reqsys.enterprise.domain.Requisito;
import br.com.reqsys.enterprise.domain.StatusRequisito;
import br.com.reqsys.enterprise.ports.AuditPort;
import br.com.reqsys.enterprise.ports.RequisitoPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class EstruturarRequisitoUseCase {

    private final RequisitoPort requisitoPort;
    private final AuditPort auditPort;

    public EstruturarRequisitoUseCase(RequisitoPort requisitoPort, AuditPort auditPort) {
        this.requisitoPort = requisitoPort;
        this.auditPort     = auditPort;
    }

    @Transactional
    public Requisito executar(String correlationId, UUID requisitoId, String atorSugerido) {
        Requisito r = requisitoPort.buscarPorId(requisitoId)
                .orElseThrow(() -> new ValidacaoNegocioException("REQUISITO_NAO_ENCONTRADO",
                        "Requisito não encontrado: " + requisitoId));

        if (r.status() == StatusRequisito.PUBLICADO_REDMINE ||
            r.status() == StatusRequisito.PUBLICADO_WIKI) {
            throw new ValidacaoNegocioException("REQUISITO_JA_PUBLICADO",
                    "Requisito já foi publicado e não pode ser reestruturado.");
        }

        String ator          = atorSugerido == null || atorSugerido.isBlank() ? "usuário" : atorSugerido;
        String historiaUsuario = "Como " + ator + ", quero " + r.titulo()
                + ", para obter valor de negócio rastreável.";
        String criteriosBdd  =
                "Dado que o sistema recebe uma solicitação válida\n" +
                "Quando o requisito '" + r.titulo() + "' for processado\n" +
                "Então o sistema deve gerar entregável testável e rastreável";

        Requisito estruturado = new Requisito(
                r.id(), r.solicitacaoId(), r.correlationId(), r.titulo(),
                historiaUsuario, criteriosBdd, "media",
                StatusRequisito.ESTRUTURADO, r.redmineIssueId(),
                r.criadoEmUtc(), null);

        Requisito salvo = requisitoPort.atualizar(estruturado);
        auditPort.registrar(correlationId, "REQUISITO_ESTRUTURADO", requisitoId.toString());
        return salvo;
    }
}

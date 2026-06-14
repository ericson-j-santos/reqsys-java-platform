package br.com.reqsys.enterprise.application;

import br.com.reqsys.common.domain.ValidacaoNegocioException;
import br.com.reqsys.enterprise.domain.Requisito;
import br.com.reqsys.enterprise.domain.Solicitacao;
import br.com.reqsys.enterprise.domain.StatusSolicitacao;
import br.com.reqsys.enterprise.ports.AuditPort;
import br.com.reqsys.enterprise.ports.RequisitoPort;
import br.com.reqsys.enterprise.ports.SolicitacaoPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ValidarRequisitoUseCase {

    private final SolicitacaoPort solicitacaoPort;
    private final RequisitoPort requisitoPort;
    private final AuditPort auditPort;

    public ValidarRequisitoUseCase(SolicitacaoPort solicitacaoPort,
                                   RequisitoPort requisitoPort,
                                   AuditPort auditPort) {
        this.solicitacaoPort = solicitacaoPort;
        this.requisitoPort   = requisitoPort;
        this.auditPort       = auditPort;
    }

    @Transactional
    public Requisito executar(String correlationId, UUID solicitacaoId) {
        Solicitacao s = solicitacaoPort.buscarPorId(solicitacaoId)
                .orElseThrow(() -> new ValidacaoNegocioException("SOLICITACAO_NAO_ENCONTRADA",
                        "Solicitação não encontrada: " + solicitacaoId));

        if (s.textoBruto().isBlank())
            throw new ValidacaoNegocioException("TEXTO_BRUTO_VAZIO", "textoBruto não pode ser vazio.");

        String titulo = s.textoBruto().lines().findFirst()
                .map(String::trim).filter(l -> !l.isBlank())
                .orElseThrow(() -> new ValidacaoNegocioException("TEXTO_INVALIDO",
                        "Não foi possível extrair título do texto."));

        Requisito requisito = requisitoPort.salvar(Requisito.validado(solicitacaoId, correlationId, titulo));
        solicitacaoPort.atualizarStatus(solicitacaoId, StatusSolicitacao.PROCESSANDO);
        auditPort.registrar(correlationId, "REQUISITO_VALIDADO", requisito.id().toString());
        return requisito;
    }
}

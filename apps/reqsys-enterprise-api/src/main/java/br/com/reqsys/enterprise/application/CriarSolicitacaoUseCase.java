package br.com.reqsys.enterprise.application;

import br.com.reqsys.common.domain.ValidacaoNegocioException;
import br.com.reqsys.enterprise.domain.Solicitacao;
import br.com.reqsys.enterprise.ports.AuditPort;
import br.com.reqsys.enterprise.ports.SolicitacaoPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CriarSolicitacaoUseCase {

    private final SolicitacaoPort solicitacaoPort;
    private final AuditPort auditPort;

    public CriarSolicitacaoUseCase(SolicitacaoPort solicitacaoPort, AuditPort auditPort) {
        this.solicitacaoPort = solicitacaoPort;
        this.auditPort = auditPort;
    }

    @Transactional
    public Solicitacao executar(String correlationId, String textoBruto, String origem) {
        if (textoBruto == null || textoBruto.isBlank())
            throw new ValidacaoNegocioException("TEXTO_BRUTO_VAZIO", "textoBruto não pode ser vazio.");

        Solicitacao salva = solicitacaoPort.salvar(Solicitacao.nova(correlationId, textoBruto, origem));
        auditPort.registrar(correlationId, "SOLICITACAO_CRIADA", salva.id().toString());
        return salva;
    }
}

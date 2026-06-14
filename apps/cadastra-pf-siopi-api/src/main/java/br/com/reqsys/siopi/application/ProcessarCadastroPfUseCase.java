package br.com.reqsys.siopi.application;

import br.com.reqsys.common.domain.ValidacaoNegocioException;
import br.com.reqsys.common.lgpd.PiiMasker;
import br.com.reqsys.siopi.web.dto.CadastroPfRequest;
import br.com.reqsys.siopi.web.dto.CadastroPfResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProcessarCadastroPfUseCase {

    @Transactional
    public CadastroPfResponse executar(String correlationId, String idempotencyKey, CadastroPfRequest request) {
        validarCorrelationId(correlationId);
        if (request.cpf() == null || request.cpf().replaceAll("\\D", "").length() != 11) {
            throw new ValidacaoNegocioException("CPF_INVALIDO", "CPF deve conter 11 dígitos.");
        }
        return new CadastroPfResponse(UUID.randomUUID().toString(), "VALIDADO", PiiMasker.cpf(request.cpf()), "Cadastro apto para orquestração.");
    }

    private void validarCorrelationId(String correlationId) {
        if (correlationId == null || correlationId.isBlank()) {
            throw new IllegalArgumentException("Header X-Correlation-Id é obrigatório.");
        }
    }

    private String normalizarAtor(String ator) {
        return ator == null || ator.isBlank() ? "usuário" : ator;
    }
}

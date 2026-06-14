package br.com.reqsys.enterprise.application;

import br.com.reqsys.enterprise.web.dto.RequisitoRequest;
import br.com.reqsys.enterprise.web.dto.RequisitoResponseLegacy;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @deprecated Use CriarSolicitacaoUseCase + ValidarRequisitoUseCase + EstruturarRequisitoUseCase
 */
@Deprecated
@Service
public class ProcessarRequisitoUseCase {

    public RequisitoResponseLegacy executar(String correlationId, String idempotencyKey, RequisitoRequest request) {
        if (correlationId == null || correlationId.isBlank())
            throw new IllegalArgumentException("Header X-Correlation-Id é obrigatório.");
        String titulo = request.textoBruto() == null || request.textoBruto().isBlank()
                ? "Requisito sem título"
                : request.textoBruto().lines().findFirst().orElse("Requisito");
        String ator = request.atorSugerido() == null || request.atorSugerido().isBlank()
                ? "usuário" : request.atorSugerido();
        return new RequisitoResponseLegacy(
                UUID.randomUUID().toString(), titulo,
                "Como " + ator + ", quero " + titulo + ", para obter valor de negócio rastreável.",
                "Dado uma entrada válida\nQuando o requisito for refinado\nEntão o sistema deve gerar requisito testável e rastreável",
                "media");
    }
}

package br.com.reqsys.enterprise.web;

import br.com.reqsys.enterprise.application.CriarSolicitacaoUseCase;
import br.com.reqsys.enterprise.domain.Solicitacao;
import br.com.reqsys.enterprise.web.dto.CriarSolicitacaoRequest;
import br.com.reqsys.enterprise.web.dto.SolicitacaoResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/solicitacoes")
public class SolicitacaoController {

    private final CriarSolicitacaoUseCase criarUseCase;

    public SolicitacaoController(CriarSolicitacaoUseCase criarUseCase) {
        this.criarUseCase = criarUseCase;
    }

    @PostMapping
    public ResponseEntity<SolicitacaoResponse> criar(
            @RequestHeader("X-Correlation-Id") String correlationId,
            @Valid @RequestBody CriarSolicitacaoRequest request) {
        Solicitacao salva = criarUseCase.executar(correlationId, request.textoBruto(), request.origem());
        return ResponseEntity.status(HttpStatus.CREATED).body(SolicitacaoResponse.de(salva));
    }
}

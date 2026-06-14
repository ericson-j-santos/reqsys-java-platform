package br.com.reqsys.formsplanner.web;

import br.com.reqsys.formsplanner.application.ProcessarSolicitacaoUseCase;
import br.com.reqsys.formsplanner.web.dto.SolicitacaoRequest;
import br.com.reqsys.formsplanner.web.dto.SolicitacaoResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/solicitacoes")
public class SolicitacaoController {
    private final ProcessarSolicitacaoUseCase useCase;

    public SolicitacaoController(ProcessarSolicitacaoUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("")
    public ResponseEntity<SolicitacaoResponse> processar(
            @RequestHeader("X-Correlation-Id") String correlationId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody SolicitacaoRequest request
    ) {
        SolicitacaoResponse response = useCase.executar(correlationId, idempotencyKey, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

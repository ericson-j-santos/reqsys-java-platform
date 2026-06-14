package br.com.reqsys.reports.web;

import br.com.reqsys.reports.application.ProcessarRelatorioUseCase;
import br.com.reqsys.reports.web.dto.RelatorioRequest;
import br.com.reqsys.reports.web.dto.RelatorioResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/relatorios")
public class RelatorioController {
    private final ProcessarRelatorioUseCase useCase;

    public RelatorioController(ProcessarRelatorioUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/solicitar")
    public ResponseEntity<RelatorioResponse> processar(
            @RequestHeader("X-Correlation-Id") String correlationId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody RelatorioRequest request
    ) {
        RelatorioResponse response = useCase.executar(correlationId, idempotencyKey, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

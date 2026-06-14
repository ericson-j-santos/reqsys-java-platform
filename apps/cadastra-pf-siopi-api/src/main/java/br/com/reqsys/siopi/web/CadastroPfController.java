package br.com.reqsys.siopi.web;

import br.com.reqsys.siopi.application.ProcessarCadastroPfUseCase;
import br.com.reqsys.siopi.web.dto.CadastroPfRequest;
import br.com.reqsys.siopi.web.dto.CadastroPfResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cadastros-pf")
public class CadastroPfController {
    private final ProcessarCadastroPfUseCase useCase;

    public CadastroPfController(ProcessarCadastroPfUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/validar")
    public ResponseEntity<CadastroPfResponse> processar(
            @RequestHeader("X-Correlation-Id") String correlationId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CadastroPfRequest request
    ) {
        CadastroPfResponse response = useCase.executar(correlationId, idempotencyKey, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

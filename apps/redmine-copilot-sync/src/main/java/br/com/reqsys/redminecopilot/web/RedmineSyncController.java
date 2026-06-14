package br.com.reqsys.redminecopilot.web;

import br.com.reqsys.redminecopilot.application.ProcessarRedmineSyncUseCase;
import br.com.reqsys.redminecopilot.web.dto.RedmineSyncRequest;
import br.com.reqsys.redminecopilot.web.dto.RedmineSyncResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/redmine")
public class RedmineSyncController {
    private final ProcessarRedmineSyncUseCase useCase;

    public RedmineSyncController(ProcessarRedmineSyncUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/issues")
    public ResponseEntity<RedmineSyncResponse> processar(
            @RequestHeader("X-Correlation-Id") String correlationId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody RedmineSyncRequest request
    ) {
        RedmineSyncResponse response = useCase.executar(correlationId, idempotencyKey, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

package br.com.reqsys.plannerteams.web;

import br.com.reqsys.plannerteams.application.ProcessarPlannerSyncUseCase;
import br.com.reqsys.plannerteams.web.dto.PlannerSyncRequest;
import br.com.reqsys.plannerteams.web.dto.PlannerSyncResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/planner")
public class PlannerSyncController {
    private final ProcessarPlannerSyncUseCase useCase;

    public PlannerSyncController(ProcessarPlannerSyncUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/sync")
    public ResponseEntity<PlannerSyncResponse> processar(
            @RequestHeader("X-Correlation-Id") String correlationId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody PlannerSyncRequest request
    ) {
        PlannerSyncResponse response = useCase.executar(correlationId, idempotencyKey, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

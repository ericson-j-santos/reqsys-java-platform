package br.com.reqsys.enterprise.web;

import br.com.reqsys.enterprise.application.EstruturarRequisitoUseCase;
import br.com.reqsys.enterprise.application.PublicarRedmineUseCase;
import br.com.reqsys.enterprise.application.ValidarRequisitoUseCase;
import br.com.reqsys.enterprise.web.dto.EstruturarRequest;
import br.com.reqsys.enterprise.web.dto.RequisitoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class RequisitoController {

    private final ValidarRequisitoUseCase validarUseCase;
    private final EstruturarRequisitoUseCase estruturarUseCase;
    private final PublicarRedmineUseCase publicarRedmineUseCase;

    public RequisitoController(ValidarRequisitoUseCase validarUseCase,
                               EstruturarRequisitoUseCase estruturarUseCase,
                               PublicarRedmineUseCase publicarRedmineUseCase) {
        this.validarUseCase        = validarUseCase;
        this.estruturarUseCase     = estruturarUseCase;
        this.publicarRedmineUseCase = publicarRedmineUseCase;
    }

    @PostMapping("/requisitos/validar")
    public ResponseEntity<RequisitoResponse> validar(
            @RequestHeader("X-Correlation-Id") String correlationId,
            @RequestParam UUID solicitacaoId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RequisitoResponse.de(validarUseCase.executar(correlationId, solicitacaoId)));
    }

    @PostMapping("/requisitos/estruturar/{id}")
    public ResponseEntity<RequisitoResponse> estruturar(
            @RequestHeader("X-Correlation-Id") String correlationId,
            @PathVariable UUID id,
            @RequestBody(required = false) EstruturarRequest request) {
        String ator = request != null ? request.atorSugerido() : null;
        return ResponseEntity.ok(RequisitoResponse.de(estruturarUseCase.executar(correlationId, id, ator)));
    }

    @PostMapping("/backlog/publicar-redmine/{id}")
    public ResponseEntity<RequisitoResponse> publicarRedmine(
            @RequestHeader("X-Correlation-Id") String correlationId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(RequisitoResponse.de(publicarRedmineUseCase.executar(correlationId, id)));
    }
}

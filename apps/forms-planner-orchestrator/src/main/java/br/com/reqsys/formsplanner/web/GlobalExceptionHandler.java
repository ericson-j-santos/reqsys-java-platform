package br.com.reqsys.formsplanner.web;

import br.com.reqsys.common.api.ErroResponse;
import br.com.reqsys.common.domain.ValidacaoNegocioException;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ValidacaoNegocioException.class)
    public ResponseEntity<ErroResponse> validacaoNegocio(ValidacaoNegocioException ex) {
        return erro(HttpStatus.UNPROCESSABLE_ENTITY, ex.getCodigo(), ex.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> beanValidation(MethodArgumentNotValidException ex) {
        List<ErroResponse.ErroCampo> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ErroResponse.ErroCampo(e.getField(), e.getDefaultMessage()))
                .toList();
        return erro(HttpStatus.BAD_REQUEST, "VALIDACAO_ENTRADA", "Entrada inválida.", erros);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> erroInesperado(Exception ex) {
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO", "Erro interno não esperado.", List.of());
    }

    private ResponseEntity<ErroResponse> erro(HttpStatus status, String codigo, String mensagem, List<ErroResponse.ErroCampo> erros) {
        String correlationId = MDC.get("correlation_id");
        return ResponseEntity.status(status).body(new ErroResponse(Instant.now(), status.value(), codigo, mensagem, correlationId, erros));
    }
}

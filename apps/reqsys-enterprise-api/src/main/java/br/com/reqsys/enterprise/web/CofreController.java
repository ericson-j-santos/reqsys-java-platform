package br.com.reqsys.enterprise.web;

import br.com.reqsys.common.idempotency.HashConteudo;
import br.com.reqsys.enterprise.domain.CofreSegredo;
import br.com.reqsys.enterprise.ports.CofrePort;
import br.com.reqsys.enterprise.web.dto.CofreSalvarRequest;
import br.com.reqsys.enterprise.web.dto.CofreSegredoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * Cofre de segredos centralizado do ReqSys.
 * Protegido por X-Cofre-Token (configurar REQSYS_COFRE_TOKEN no ambiente).
 * Nunca retorna o valor do segredo em claro.
 */
@RestController
@RequestMapping("/api/v1/cofre")
public class CofreController {

    private final CofrePort cofrePort;
    private final String cofreToken;

    public CofreController(CofrePort cofrePort,
                           @Value("${reqsys.cofre.token:}") String cofreToken) {
        this.cofrePort = cofrePort;
        this.cofreToken = cofreToken;
    }

    @GetMapping("/segredo/{chave}")
    public ResponseEntity<CofreSegredoResponse> buscar(
            @PathVariable("chave") String chave,
            @RequestHeader(value = "X-Cofre-Token", required = false) String token) {
        validarToken(token);
        return cofrePort.buscarPorChave(chave)
                .map(s -> ResponseEntity.ok(toResponse(s)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/segredos")
    public ResponseEntity<List<CofreSegredoResponse>> listarPorSistema(
            @RequestParam("sistema") String sistema,
            @RequestHeader(value = "X-Cofre-Token", required = false) String token) {
        validarToken(token);
        List<CofreSegredoResponse> resultado = cofrePort.listarPorSistema(sistema)
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/segredo")
    public ResponseEntity<CofreSegredoResponse> salvar(
            @RequestBody CofreSalvarRequest request,
            @RequestHeader(value = "X-Cofre-Token", required = false) String token) {
        validarToken(token);
        CofreSegredo salvo = cofrePort.salvar(request.chave(), request.valor(), request.sistema(), request.descricao());
        return ResponseEntity.ok(toResponse(salvo));
    }

    private void validarToken(String token) {
        if (cofreToken == null || cofreToken.isBlank()) {
            throw new SecurityException("REQSYS_COFRE_TOKEN nao configurado. Cofre bloqueado.");
        }
        byte[] esperado = cofreToken.getBytes(StandardCharsets.UTF_8);
        byte[] recebido = token == null ? new byte[0] : token.getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(esperado, recebido)) {
            throw new SecurityException("X-Cofre-Token invalido ou ausente.");
        }
    }

    private CofreSegredoResponse toResponse(CofreSegredo s) {
        String valor = s.valor() == null ? "" : s.valor();
        String fingerprint = HashConteudo.sha256(s.chave() + ":" + s.sistema() + ":" + valor).substring(0, 16);
        return new CofreSegredoResponse(
                s.chave(),
                !valor.isBlank(),
                fingerprint,
                s.sistema(),
                s.descricao());
    }
}

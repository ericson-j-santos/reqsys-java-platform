package br.com.reqsys.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public class MicrosoftGraphTokenClient {

    private static final Logger log = LoggerFactory.getLogger(MicrosoftGraphTokenClient.class);
    private static final long MARGEM_EXPIRACAO_SEGUNDOS = 300L;

    private final MicrosoftGraphProperties properties;
    private final WebClient webClient;
    private TokenCache cache;

    public MicrosoftGraphTokenClient(MicrosoftGraphProperties properties, WebClient.Builder webClientBuilder) {
        this.properties = Objects.requireNonNull(properties, "properties não pode ser nulo");
        this.webClient = webClientBuilder.build();
    }

    public synchronized String obterAccessToken() {
        validarConfiguracao();

        if (cache != null && cache.expiraEm().isAfter(Instant.now().plusSeconds(MARGEM_EXPIRACAO_SEGUNDOS))) {
            return cache.accessToken();
        }

        String tokenUrl = "https://login.microsoftonline.com/" + properties.getTenantId() + "/oauth2/v2.0/token";

        TokenResponse response = webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters
                        .fromFormData("client_id", properties.getClientId())
                        .with("client_secret", properties.getClientSecret())
                        .with("scope", properties.getScope())
                        .with("grant_type", "client_credentials"))
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .block();

        if (response == null || response.access_token() == null || response.access_token().isBlank()) {
            throw new IllegalStateException("Microsoft Graph não retornou access_token.");
        }

        long expiresIn = response.expires_in() == null ? 3600L : response.expires_in();
        this.cache = new TokenCache(response.access_token(), Instant.now().plusSeconds(expiresIn));
        log.info("Token Microsoft Graph obtido com sucesso. Expira em {} segundos.", expiresIn);
        return cache.accessToken();
    }

    private void validarConfiguracao() {
        Map<String, String> obrigatorios = Map.of(
                "tenantId", properties.getTenantId(),
                "clientId", properties.getClientId(),
                "clientSecret", properties.getClientSecret()
        );

        obrigatorios.forEach((nome, valor) -> {
            if (valor == null || valor.isBlank()) {
                throw new IllegalStateException("Configuração obrigatória ausente para Microsoft Graph: " + nome);
            }
        });
    }

    private record TokenResponse(String access_token, Long expires_in, String token_type) {}

    private record TokenCache(String accessToken, Instant expiraEm) {}
}

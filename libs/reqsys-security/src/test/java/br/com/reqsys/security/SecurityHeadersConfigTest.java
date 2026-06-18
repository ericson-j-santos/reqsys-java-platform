package br.com.reqsys.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityHeadersConfigTest {

    @Test
    void deveRejeitarJwtDecoderSemIssuerQuandoSegurancaAtiva() {
        SecurityHeadersConfig config = new SecurityHeadersConfig();

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> config.jwtDecoder("", "api://reqsys"));

        assertTrue(ex.getMessage().contains("issuer-uri obrigatorio"));
    }

    @Test
    void deveCriarJwtDecoderComIssuer() {
        SecurityHeadersConfig config = new SecurityHeadersConfig();

        JwtDecoder decoder = assertDoesNotThrow(() ->
                config.jwtDecoder("https://login.microsoftonline.com/tenant/v2.0", "api://reqsys"));

        assertTrue(decoder != null);
    }

    @Test
    void deveConverterScopesERolesEmAuthorities() {
        SecurityHeadersConfig config = new SecurityHeadersConfig();
        JwtAuthenticationConverter converter = config.jwtAuthenticationConverter();

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .issuer("https://login.microsoftonline.com/tenant/v2.0")
                .audience(List.of("api://reqsys"))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .claim("scope", "requisitos.read requisitos.write")
                .claim("roles", List.of("ADMIN"))
                .build();

        var authentication = converter.convert(jwt);

        assertTrue(authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("SCOPE_requisitos.read")));
        assertTrue(authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("SCOPE_requisitos.write")));
        assertTrue(authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }
}

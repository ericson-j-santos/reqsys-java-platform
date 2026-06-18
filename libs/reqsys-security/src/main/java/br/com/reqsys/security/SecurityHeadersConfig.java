package br.com.reqsys.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
@EnableMethodSecurity
public class SecurityHeadersConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            @Value("${reqsys.security.enabled:true}") boolean securityEnabled,
            @Value("${reqsys.cors.allowed-origins:}") String allowedOriginsCsv) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; frame-ancestors 'none'; object-src 'none'"))
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).preload(true)))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/actuator/health", "/actuator/info").permitAll();
                    if (securityEnabled) {
                        auth.anyRequest().authenticated();
                    } else {
                        auth.anyRequest().permitAll();
                    }
                });

        if (!parseCsv(allowedOriginsCsv).isEmpty()) {
            http.cors(cors -> cors.configurationSource(corsConfigurationSource(allowedOriginsCsv)));
        }

        if (securityEnabled) {
            http.oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
        }

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Set<GrantedAuthority> authorities = new LinkedHashSet<>();
            adicionarScopes(authorities, jwt.getClaimAsString("scope"));
            adicionarColecao(authorities, jwt.getClaim("scp"), "SCOPE_");
            adicionarColecao(authorities, jwt.getClaim("roles"), "ROLE_");
            adicionarRealmRoles(authorities, jwt);
            return authorities;
        });
        return converter;
    }

    @Bean
    @ConditionalOnProperty(name = "reqsys.security.enabled", havingValue = "true", matchIfMissing = true)
    public JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}") String issuerUri,
            @Value("${reqsys.security.jwt.audiences:}") String audiencesCsv) {

        if (issuerUri == null || issuerUri.isBlank()) {
            throw new IllegalStateException("JWT issuer-uri obrigatorio quando reqsys.security.enabled=true.");
        }

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
        OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuerUri);
        Set<String> audiences = parseCsv(audiencesCsv);

        if (audiences.isEmpty()) {
            decoder.setJwtValidator(issuerValidator);
        } else {
            decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(issuerValidator, new AudienceValidator(audiences)));
        }

        return decoder;
    }

    private CorsConfigurationSource corsConfigurationSource(String allowedOriginsCsv) {
        List<String> origins = new ArrayList<>(parseCsv(allowedOriginsCsv));
        return request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOrigins(origins);
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
            config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Correlation-Id", "Idempotency-Key", "X-Cofre-Token"));
            config.setExposedHeaders(List.of("X-Correlation-Id"));
            config.setAllowCredentials(true);
            config.setMaxAge(3600L);
            return config;
        };
    }

    private static void adicionarScopes(Set<GrantedAuthority> authorities, String scopes) {
        if (scopes == null || scopes.isBlank()) return;
        for (String scope : scopes.split("\\s+")) {
            if (!scope.isBlank()) authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope.trim()));
        }
    }

    private static void adicionarColecao(Set<GrantedAuthority> authorities, Object claim, String prefixo) {
        if (!(claim instanceof Collection<?> valores)) return;
        valores.stream()
                .map(String::valueOf)
                .filter(v -> !v.isBlank())
                .map(v -> new SimpleGrantedAuthority(prefixo + v.trim()))
                .forEach(authorities::add);
    }

    @SuppressWarnings("unchecked")
    private static void adicionarRealmRoles(Set<GrantedAuthority> authorities, Jwt jwt) {
        Object realmAccess = jwt.getClaim("realm_access");
        if (realmAccess instanceof Map<?, ?> map) {
            adicionarColecao(authorities, map.get("roles"), "ROLE_");
        }
    }

    private static Set<String> parseCsv(String csv) {
        Set<String> valores = new LinkedHashSet<>();
        if (csv == null || csv.isBlank()) return valores;
        for (String item : csv.split(",")) {
            String normalizado = item.trim();
            if (!normalizado.isBlank()) valores.add(normalizado);
        }
        return valores;
    }

    private static final class AudienceValidator implements OAuth2TokenValidator<Jwt> {
        private final Set<String> allowedAudiences;

        private AudienceValidator(Set<String> allowedAudiences) {
            this.allowedAudiences = allowedAudiences;
        }

        @Override
        public OAuth2TokenValidatorResult validate(Jwt token) {
            List<String> tokenAudiences = token.getAudience();
            boolean valido = tokenAudiences != null && tokenAudiences.stream().anyMatch(allowedAudiences::contains);
            if (valido) return OAuth2TokenValidatorResult.success();
            OAuth2Error error = new OAuth2Error("invalid_token", "JWT audience invalida para o ReqSys.", null);
            return OAuth2TokenValidatorResult.failure(error);
        }
    }
}

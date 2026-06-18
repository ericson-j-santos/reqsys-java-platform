package br.com.reqsys.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Objects;

public class MicrosoftGraphTeamsAdapter implements TeamsPort {

    private static final Logger log = LoggerFactory.getLogger(MicrosoftGraphTeamsAdapter.class);

    private final MicrosoftGraphProperties properties;
    private final MicrosoftGraphTokenClient tokenClient;
    private final WebClient webClient;

    public MicrosoftGraphTeamsAdapter(
            MicrosoftGraphProperties properties,
            MicrosoftGraphTokenClient tokenClient,
            WebClient.Builder webClientBuilder
    ) {
        this.properties = Objects.requireNonNull(properties, "properties não pode ser nulo");
        this.tokenClient = Objects.requireNonNull(tokenClient, "tokenClient não pode ser nulo");
        this.webClient = webClientBuilder.baseUrl(properties.getGraphBaseUrl()).build();
    }

    @Override
    public void enviarMensagemUsuario(String emailDestinatario, String mensagemMarkdown) {
        validarEmail(emailDestinatario);
        validarMensagem(mensagemMarkdown);

        String token = tokenClient.obterAccessToken();
        String chatId = criarChatUmParaUm(emailDestinatario, token);
        enviarMensagemChat(chatId, mensagemMarkdown, token);

        log.info("Mensagem Teams enviada via Microsoft Graph para destinatario={}", mascararEmail(emailDestinatario));
    }

    @Override
    public boolean usuarioExiste(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return false;
        }

        try {
            String token = tokenClient.obterAccessToken();
            webClient.get()
                    .uri("/users/{email}?$select=id,mail,userPrincipalName", email)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return true;
        } catch (RuntimeException ex) {
            log.warn("Usuário Teams/Microsoft Graph não localizado ou sem permissão. email={}", mascararEmail(email));
            return false;
        }
    }

    private String criarChatUmParaUm(String emailDestinatario, String token) {
        Map<String, Object> body = Map.of(
                "chatType", "oneOnOne",
                "members", new Object[]{
                        Map.of(
                                "@odata.type", "#microsoft.graph.aadUserConversationMember",
                                "roles", new String[]{"owner"},
                                "user@odata.bind", properties.getGraphBaseUrl() + "/users/" + emailDestinatario
                        )
                }
        );

        Map<?, ?> response = webClient.post()
                .uri("/chats")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        Object id = response == null ? null : response.get("id");
        if (id == null || id.toString().isBlank()) {
            throw new IllegalStateException("Microsoft Graph não retornou chatId ao criar chat Teams.");
        }
        return id.toString();
    }

    private void enviarMensagemChat(String chatId, String mensagemMarkdown, String token) {
        Map<String, Object> body = Map.of(
                "body", Map.of(
                        "contentType", "html",
                        "content", markdownBasicoParaHtml(mensagemMarkdown)
                )
        );

        webClient.post()
                .uri("/chats/{chatId}/messages", chatId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    private void validarEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("E-mail destinatário inválido para Teams.");
        }
    }

    private void validarMensagem(String mensagemMarkdown) {
        if (mensagemMarkdown == null || mensagemMarkdown.isBlank()) {
            throw new IllegalArgumentException("Mensagem Teams não pode ser vazia.");
        }
    }

    private String markdownBasicoParaHtml(String markdown) {
        return markdown
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br/>")
                .replace("## ", "<strong>")
                .replace("`", "");
    }

    private String mascararEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] partes = email.split("@", 2);
        String usuario = partes[0];
        String dominio = partes[1];
        String usuarioMascarado = usuario.length() <= 2 ? "**" : usuario.substring(0, 2) + "***";
        return usuarioMascarado + "@" + dominio;
    }
}

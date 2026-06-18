package br.com.reqsys.graph;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "reqsys.microsoft.graph")
public class MicrosoftGraphProperties {

    /**
     * Tenant ID do Microsoft Entra ID.
     */
    private String tenantId;

    /**
     * Client ID da App Registration.
     */
    private String clientId;

    /**
     * Client secret carregado exclusivamente por variável de ambiente ou secret manager.
     */
    private String clientSecret;

    /**
     * Base URL do Microsoft Graph.
     */
    private String graphBaseUrl = "https://graph.microsoft.com/v1.0";

    /**
     * Scope padrão para client credentials.
     */
    private String scope = "https://graph.microsoft.com/.default";

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getGraphBaseUrl() {
        return graphBaseUrl;
    }

    public void setGraphBaseUrl(String graphBaseUrl) {
        this.graphBaseUrl = graphBaseUrl;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}

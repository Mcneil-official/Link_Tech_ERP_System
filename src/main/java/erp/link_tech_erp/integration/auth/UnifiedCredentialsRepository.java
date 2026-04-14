package erp.link_tech_erp.integration.auth;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import erp.link_tech_erp.finance.repository.SupabaseRestClient;
import erp.link_tech_erp.finance.repository.SupabaseRestConfig;

public final class UnifiedCredentialsRepository {
    private final SupabaseRestClient restClient;
    private final ObjectMapper objectMapper;

    public UnifiedCredentialsRepository() {
        this.restClient = new SupabaseRestClient(SupabaseRestConfig.fromEnvironment());
        this.objectMapper = new ObjectMapper();
    }

    public GlobalSession authenticate(String loginIdentifier, String password) {
        String normalizedIdentifier = loginIdentifier == null ? "" : loginIdentifier.trim();
        if (normalizedIdentifier.isBlank() || password == null || password.isBlank()) {
            return null;
        }

        String path = "/users?select=username,module_code"
            + "&username=eq." + enc(normalizedIdentifier)
            + "&password=eq." + enc(password)
            + "&limit=1";

        String responseBody = restClient.get(path);
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            if (!root.isArray() || root.isEmpty()) {
                return null;
            }

            JsonNode record = root.get(0);
            String username = record.path("username").asText(normalizedIdentifier);
            String moduleCode = record.path("module_code").asText("");
            ModuleAccess moduleAccess = resolveModule(moduleCode);
            if (moduleAccess == null) {
                return null;
            }

            return new GlobalSession(moduleAccess, username, username);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to parse authentication response.", exception);
        }
    }

    private ModuleAccess resolveModule(String moduleCode) {
        if (moduleCode == null || moduleCode.isBlank()) {
            return null;
        }

        try {
            return ModuleAccess.valueOf(moduleCode.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
            .replace("+", "%20");
    }
}
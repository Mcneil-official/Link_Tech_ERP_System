package erp.link_tech_erp.finance.repository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserRoleRepository {
    private final SupabaseRestClient restClient;
    private final ObjectMapper objectMapper;

    public UserRoleRepository(SupabaseRestConfig config) {
        this.restClient = new SupabaseRestClient(config);
        this.objectMapper = new ObjectMapper();
    }

    public Optional<String> findRoleByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        String encoded = URLEncoder.encode(email.trim().toLowerCase(), StandardCharsets.UTF_8);
        String response = restClient.get("/user_roles?select=role_name&user_email=eq." + encoded + "&limit=1");

        try {
            JsonNode root = objectMapper.readTree(response);
            if (!root.isArray() || root.isEmpty()) {
                return Optional.empty();
            }
            JsonNode roleNode = root.get(0).get("role_name");
            if (roleNode == null || roleNode.isNull() || roleNode.asText().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(roleNode.asText());
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to parse user role response.", exception);
        }
    }
}

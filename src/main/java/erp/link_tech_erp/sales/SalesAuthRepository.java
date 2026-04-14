package erp.link_tech_erp.sales;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class SalesAuthRepository {

    private final SalesSupabaseRestClient restClient;
    private final ObjectMapper objectMapper;

    public SalesAuthRepository() {
        this.restClient = new SalesSupabaseRestClient(SalesSupabaseRestConfig.fromEnvironment());
        this.objectMapper = new ObjectMapper();
    }

    public void ensureDefaultAdminExists(String username, String password) {
        if (userExists(username)) {
            return;
        }

        try {
            String payload = objectMapper.writeValueAsString(new LoginUserRequest(username, password));
            restClient.post("users", payload, false);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize default admin payload.", exception);
        }
    }

    public boolean isValidUser(String username, String password) {
        String path = "/users?select=user_id"
            + "&username=eq." + SalesSupabaseRestClient.enc(username)
            + "&password=eq." + SalesSupabaseRestClient.enc(password)
            + "&limit=1";
        JsonNode root = restClient.get(path);
        return root.isArray() && root.size() > 0;
    }

    private boolean userExists(String username) {
        String path = "/users?select=user_id"
            + "&username=eq." + SalesSupabaseRestClient.enc(username)
            + "&limit=1";
        JsonNode root = restClient.get(path);
        return root.isArray() && root.size() > 0;
    }

    private record LoginUserRequest(String username, String password) {
    }
}

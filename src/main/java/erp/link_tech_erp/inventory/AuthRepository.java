package erp.link_tech_erp.inventory;

import com.fasterxml.jackson.databind.JsonNode;

public class AuthRepository {

    private final InventorySupabaseRestClient restClient =
        new InventorySupabaseRestClient(InventorySupabaseRestConfig.fromEnvironment());

    public String authenticate(String username, String password) {
        try {
            String endpoint = "/app_users?select=role"
                + "&username=eq." + InventorySupabaseRestClient.enc(username)
                + "&password=eq." + InventorySupabaseRestClient.enc(password)
                + "&limit=1";
            JsonNode rows = restClient.get(endpoint);
            if (rows.isArray() && !rows.isEmpty()) {
                return rows.get(0).path("role").asText(null);
            }
            return null;
        } catch (Exception exception) {
            throw new RuntimeException("Failed to authenticate against Supabase REST API.", exception);
        }
    }

    public RememberedLogin loadRememberedLogin() {
        try {
            String endpoint = "/app_preferences?select=remember_me,remembered_username&id=eq.1&limit=1";
            JsonNode rows = restClient.get(endpoint);
            if (rows.isArray() && !rows.isEmpty()) {
                JsonNode row = rows.get(0);
                return new RememberedLogin(
                    row.path("remember_me").asBoolean(false),
                    row.path("remembered_username").asText("")
                );
            }
        } catch (Exception exception) {
            throw new RuntimeException("Failed to load remembered login from Supabase REST API.", exception);
        }

        return new RememberedLogin(false, "");
    }

    public void saveRememberedLogin(boolean rememberMe, String username) {
        String remembered = rememberMe ? username : null;
        String payload = "{"
            + "\"remember_me\":" + rememberMe + ","
            + "\"remembered_username\":" + (remembered == null ? "null" : "\"" + escape(remembered) + "\"")
            + "}";

        try {
            restClient.patch("/app_preferences?id=eq.1", payload, false);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to save remembered login to Supabase REST API.", exception);
        }
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static final class RememberedLogin {
        private final boolean rememberMe;
        private final String username;

        public RememberedLogin(boolean rememberMe, String username) {
            this.rememberMe = rememberMe;
            this.username = username == null ? "" : username;
        }

        public boolean isRememberMe() {
            return rememberMe;
        }

        public String getUsername() {
            return username;
        }
    }
}

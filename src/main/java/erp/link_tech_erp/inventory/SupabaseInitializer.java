package erp.link_tech_erp.inventory;

import com.fasterxml.jackson.databind.JsonNode;

public final class SupabaseInitializer {

    private SupabaseInitializer() {
    }

    public static void initialize() {
        try {
            InventorySupabaseRestClient client =
                new InventorySupabaseRestClient(InventorySupabaseRestConfig.fromEnvironment());

            // Validate required tables exist and REST API is reachable.
            client.get("/products?select=id&limit=1");
            client.get("/suppliers?select=id&limit=1");
            client.get("/purchase_orders?select=id&limit=1");

            seedUsersIfEmpty(client);
            seedPreferencesIfEmpty(client);
        } catch (Exception exception) {
            throw new RuntimeException(
                "Failed to initialize Inventory against Supabase REST API: " + exception.getMessage()
                    + ". Ensure tables products, suppliers, purchase_orders, app_users, and app_preferences exist.",
                exception
            );
        }
    }

    private static void seedUsersIfEmpty(InventorySupabaseRestClient client) {
        JsonNode rows = client.get("/app_users?select=username&limit=1");
        if (rows.isArray() && rows.isEmpty()) {
            String payload = "["
                + "{\"username\":\"admin\",\"password\":\"admin123\",\"role\":\"Administrator\"},"
                + "{\"username\":\"manager\",\"password\":\"manager123\",\"role\":\"Inventory Manager\"},"
                + "{\"username\":\"staff\",\"password\":\"staff123\",\"role\":\"Inventory Staff\"}"
                + "]";
            client.post("app_users", payload, false);
        }
    }

    private static void seedPreferencesIfEmpty(InventorySupabaseRestClient client) {
        JsonNode rows = client.get("/app_preferences?select=id&id=eq.1&limit=1");
        if (rows.isArray() && rows.isEmpty()) {
            client.post("app_preferences", "{\"id\":1,\"remember_me\":false,\"remembered_username\":null}", false);
        }
    }
}

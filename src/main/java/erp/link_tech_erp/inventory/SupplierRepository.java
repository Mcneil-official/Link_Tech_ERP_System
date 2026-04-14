package erp.link_tech_erp.inventory;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public class SupplierRepository {

    private final InventorySupabaseRestClient restClient =
        new InventorySupabaseRestClient(InventorySupabaseRestConfig.fromEnvironment());

    public List<Supplier> findAll() {
        List<Supplier> suppliers = new ArrayList<>();
        String endpoint = "/suppliers?select=id,name,category,email,phone,address,rating&order=id.asc";

        try {
            JsonNode rows = restClient.get(endpoint);
            for (JsonNode row : rows) {
                suppliers.add(new Supplier(
                    row.path("id").asInt(),
                    row.path("name").asText(""),
                    row.path("category").asText(""),
                    row.path("email").asText(""),
                    row.path("phone").asText(""),
                    row.path("address").asText(""),
                    row.path("rating").asText("")
                ));
            }
        } catch (Exception exception) {
            throw new RuntimeException("Failed to load suppliers from Supabase REST API.", exception);
        }

        return suppliers;
    }

    public Supplier insert(Supplier supplier) {
        String payload = "{"
            + "\"name\":\"" + escape(supplier.getName()) + "\"," 
            + "\"category\":\"" + escape(supplier.getCategory()) + "\"," 
            + "\"email\":\"" + escape(supplier.getEmail()) + "\"," 
            + "\"phone\":\"" + escape(supplier.getPhone()) + "\"," 
            + "\"address\":\"" + escape(supplier.getAddress()) + "\"," 
            + "\"rating\":\"" + escape(supplier.getRating()) + "\""
            + "}";

        try {
            JsonNode rows = restClient.post("suppliers", payload, true);
            if (rows.isArray() && !rows.isEmpty()) {
                JsonNode row = rows.get(0);
                return new Supplier(
                    row.path("id").asInt(),
                    row.path("name").asText(supplier.getName()),
                    row.path("category").asText(supplier.getCategory()),
                    row.path("email").asText(supplier.getEmail()),
                    row.path("phone").asText(supplier.getPhone()),
                    row.path("address").asText(supplier.getAddress()),
                    row.path("rating").asText(supplier.getRating())
                );
            }
        } catch (Exception exception) {
            throw new RuntimeException("Failed to insert supplier into Supabase REST API.", exception);
        }

        throw new RuntimeException("Failed to insert supplier into Supabase REST API: no id returned.");
    }

    public void update(Supplier supplier) {
        String payload = "{"
            + "\"name\":\"" + escape(supplier.getName()) + "\"," 
            + "\"category\":\"" + escape(supplier.getCategory()) + "\"," 
            + "\"email\":\"" + escape(supplier.getEmail()) + "\"," 
            + "\"phone\":\"" + escape(supplier.getPhone()) + "\"," 
            + "\"address\":\"" + escape(supplier.getAddress()) + "\"," 
            + "\"rating\":\"" + escape(supplier.getRating()) + "\""
            + "}";

        try {
            restClient.patch("/suppliers?id=eq." + supplier.getId(), payload, false);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to update supplier in Supabase REST API.", exception);
        }
    }

    public void deleteById(int id) {
        try {
            restClient.delete("/suppliers?id=eq." + id, false);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to delete supplier from Supabase REST API.", exception);
        }
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

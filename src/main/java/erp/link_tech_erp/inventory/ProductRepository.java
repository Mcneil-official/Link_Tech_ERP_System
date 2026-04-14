package erp.link_tech_erp.inventory;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public class ProductRepository {

    private final InventorySupabaseRestClient restClient =
        new InventorySupabaseRestClient(InventorySupabaseRestConfig.fromEnvironment());

    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String endpoint = "/products?select=id,name,category,stock,unit_price,supplier_id,supplier_name&order=id.asc";

        try {
            JsonNode rows = restClient.get(endpoint);
            for (JsonNode row : rows) {
                products.add(new Product(
                    row.path("id").asInt(),
                    row.path("name").asText(""),
                    row.path("category").asText(""),
                    row.path("stock").asInt(),
                    row.path("unit_price").asDouble(),
                    row.path("supplier_id").asInt(),
                    row.path("supplier_name").asText("")
                ));
            }
        } catch (Exception exception) {
            throw new RuntimeException("Failed to load products from Supabase REST API.", exception);
        }

        return products;
    }

    public Product insert(Product product) {
        String payload = "{"
            + "\"name\":\"" + escape(product.getName()) + "\"," 
            + "\"category\":\"" + escape(product.getCategory()) + "\"," 
            + "\"stock\":" + product.getStock() + ","
            + "\"unit_price\":" + product.getUnitPrice() + ","
            + "\"supplier_id\":" + product.getSupplierId() + ","
            + "\"supplier_name\":\"" + escape(product.getSupplierName()) + "\""
            + "}";

        try {
            JsonNode rows = restClient.post("products", payload, true);
            if (rows.isArray() && !rows.isEmpty()) {
                JsonNode row = rows.get(0);
                return new Product(
                    row.path("id").asInt(),
                    row.path("name").asText(product.getName()),
                    row.path("category").asText(product.getCategory()),
                    row.path("stock").asInt(product.getStock()),
                    row.path("unit_price").asDouble(product.getUnitPrice()),
                    row.path("supplier_id").asInt(product.getSupplierId()),
                    row.path("supplier_name").asText(product.getSupplierName())
                );
            }
        } catch (Exception exception) {
            throw new RuntimeException("Failed to insert product into Supabase REST API.", exception);
        }

        throw new RuntimeException("Failed to insert product into Supabase REST API: no id returned.");
    }

    public void update(Product product) {
        String payload = "{"
            + "\"name\":\"" + escape(product.getName()) + "\"," 
            + "\"category\":\"" + escape(product.getCategory()) + "\"," 
            + "\"stock\":" + product.getStock() + ","
            + "\"unit_price\":" + product.getUnitPrice() + ","
            + "\"supplier_id\":" + product.getSupplierId() + ","
            + "\"supplier_name\":\"" + escape(product.getSupplierName()) + "\""
            + "}";

        try {
            restClient.patch("/products?id=eq." + product.getId(), payload, false);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to update product in Supabase REST API.", exception);
        }
    }

    public void deleteById(int id) {
        try {
            restClient.delete("/products?id=eq." + id, false);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to delete product from Supabase REST API.", exception);
        }
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

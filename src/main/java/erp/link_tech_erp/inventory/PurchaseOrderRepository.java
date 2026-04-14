package erp.link_tech_erp.inventory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public class PurchaseOrderRepository {

    private final InventorySupabaseRestClient restClient =
        new InventorySupabaseRestClient(InventorySupabaseRestConfig.fromEnvironment());

    public List<PurchaseOrder> findAll() {
        List<PurchaseOrder> orders = new ArrayList<>();
        String endpoint = "/purchase_orders?select=id,supplier_name,quantity,unit_price,status,order_date&order=id.asc";

        try {
            JsonNode rows = restClient.get(endpoint);
            for (JsonNode row : rows) {
                String dateValue = row.path("order_date").asText(LocalDate.now().toString());
                orders.add(new PurchaseOrder(
                    row.path("id").asInt(),
                    row.path("supplier_name").asText(""),
                    row.path("quantity").asInt(),
                    row.path("unit_price").asDouble(),
                    row.path("status").asText(""),
                    LocalDate.parse(dateValue)
                ));
            }
        } catch (Exception exception) {
            throw new RuntimeException("Failed to load purchase orders from Supabase REST API.", exception);
        }

        return orders;
    }

    public PurchaseOrder insert(PurchaseOrder order) {
        String payload = "{"
            + "\"supplier_name\":\"" + escape(order.getSupplier()) + "\"," 
            + "\"quantity\":" + order.getQuantity() + ","
            + "\"unit_price\":" + order.getUnitPrice() + ","
            + "\"status\":\"" + escape(order.getStatus()) + "\"," 
            + "\"order_date\":\"" + order.getOrderDate() + "\""
            + "}";

        try {
            JsonNode rows = restClient.post("purchase_orders", payload, true);
            if (rows.isArray() && !rows.isEmpty()) {
                JsonNode row = rows.get(0);
                String dateValue = row.path("order_date").asText(order.getOrderDate().toString());
                return new PurchaseOrder(
                    row.path("id").asInt(),
                    row.path("supplier_name").asText(order.getSupplier()),
                    row.path("quantity").asInt(order.getQuantity()),
                    row.path("unit_price").asDouble(order.getUnitPrice()),
                    row.path("status").asText(order.getStatus()),
                    LocalDate.parse(dateValue)
                );
            }
        } catch (Exception exception) {
            throw new RuntimeException("Failed to insert purchase order into Supabase REST API.", exception);
        }

        throw new RuntimeException("Failed to insert purchase order into Supabase REST API: no id returned.");
    }

    public void update(PurchaseOrder order) {
        String payload = "{"
            + "\"supplier_name\":\"" + escape(order.getSupplier()) + "\"," 
            + "\"quantity\":" + order.getQuantity() + ","
            + "\"unit_price\":" + order.getUnitPrice() + ","
            + "\"status\":\"" + escape(order.getStatus()) + "\"," 
            + "\"order_date\":\"" + order.getOrderDate() + "\""
            + "}";

        try {
            restClient.patch("/purchase_orders?id=eq." + order.getOrderId(), payload, false);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to update purchase order in Supabase REST API.", exception);
        }
    }

    public void deleteById(int id) {
        try {
            restClient.delete("/purchase_orders?id=eq." + id, false);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to delete purchase order from Supabase REST API.", exception);
        }
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

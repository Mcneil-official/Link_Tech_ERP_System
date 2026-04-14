package erp.link_tech_erp.sales;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class SalesOrderRepository {

    private static final String SELECT_FIELDS =
        "order_id,customer_name,product_name,quantity,total_price,payment_status,delivery_status,order_date";

    private final SalesSupabaseRestClient restClient;
    private final ObjectMapper objectMapper;

    public SalesOrderRepository() {
        this.restClient = new SalesSupabaseRestClient(SalesSupabaseRestConfig.fromEnvironment());
        this.objectMapper = new ObjectMapper();
    }

    public List<SalesOrder> listOrders() {
        JsonNode root = restClient.get("/orders?select=" + SELECT_FIELDS + "&order=order_id.desc");
        return mapOrders(root);
    }

    public List<SalesOrder> listOrdersByDeliveryStatus(String deliveryStatus) {
        String path = "/orders?select=" + SELECT_FIELDS
            + "&delivery_status=eq." + SalesSupabaseRestClient.enc(deliveryStatus)
            + "&order=order_id.desc";
        JsonNode root = restClient.get(path);
        return mapOrders(root);
    }

    public List<SalesOrder> listRecentOrders(int limit) {
        int safeLimit = Math.max(1, limit);
        String path = "/orders?select=" + SELECT_FIELDS + "&order=order_id.desc&limit=" + safeLimit;
        JsonNode root = restClient.get(path);
        return mapOrders(root);
    }

    public void createOrder(
            String customerName,
            String productName,
            int quantity,
            double totalPrice,
            String paymentStatus,
            String deliveryStatus) {
        try {
            String payload = objectMapper.writeValueAsString(new CreateOrderRequest(
                customerName,
                productName,
                quantity,
                totalPrice,
                paymentStatus,
                deliveryStatus
            ));
            restClient.post("orders", payload, false);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize order payload.", exception);
        }
    }

    public boolean deleteOrder(int orderId) {
        JsonNode deleted = restClient.delete("/orders?order_id=eq." + orderId, true);
        return deleted.isArray() && deleted.size() > 0;
    }

    public String getPaymentStatus(int orderId) {
        JsonNode root = restClient.get("/orders?select=payment_status&order_id=eq." + orderId + "&limit=1");
        if (root.isArray() && root.size() > 0) {
            return root.get(0).path("payment_status").asText("");
        }
        return "";
    }

    public boolean updateDeliveryStatus(int orderId, String status) {
        return updateField(orderId, "delivery_status", status);
    }

    public boolean updatePaymentStatus(int orderId, String status) {
        return updateField(orderId, "payment_status", status);
    }

    private boolean updateField(int orderId, String fieldName, String value) {
        try {
            String payload = objectMapper.writeValueAsString(new UpdateFieldRequest(fieldName, value));
            JsonNode updated = restClient.patch("/orders?order_id=eq." + orderId, payload, true);
            return updated.isArray() && updated.size() > 0;
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize order update payload.", exception);
        }
    }

    private List<SalesOrder> mapOrders(JsonNode root) {
        List<SalesOrder> orders = new ArrayList<>();
        if (!root.isArray()) {
            return orders;
        }

        for (JsonNode node : root) {
            orders.add(SalesOrder.fromJson(node));
        }
        return orders;
    }

    private record CreateOrderRequest(
            String customer_name,
            String product_name,
            int quantity,
            double total_price,
            String payment_status,
            String delivery_status) {
    }

    private static final class UpdateFieldRequest {
        private String delivery_status;
        private String payment_status;

        private UpdateFieldRequest(String fieldName, String value) {
            if ("delivery_status".equals(fieldName)) {
                this.delivery_status = value;
            } else if ("payment_status".equals(fieldName)) {
                this.payment_status = value;
            }
        }

        public String getDelivery_status() {
            return delivery_status;
        }

        public String getPayment_status() {
            return payment_status;
        }
    }
}

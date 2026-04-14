package erp.link_tech_erp.sales;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.databind.JsonNode;

public final class SalesOrder {

    private final int orderId;
    private final String customerName;
    private final String productName;
    private final int quantity;
    private final double totalPrice;
    private final String paymentStatus;
    private final String deliveryStatus;
    private final Timestamp orderDate;

    public SalesOrder(
            int orderId,
            String customerName,
            String productName,
            int quantity,
            double totalPrice,
            String paymentStatus,
            String deliveryStatus,
            Timestamp orderDate) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.productName = productName;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.paymentStatus = paymentStatus;
        this.deliveryStatus = deliveryStatus;
        this.orderDate = orderDate;
    }

    public static SalesOrder fromJson(JsonNode node) {
        return new SalesOrder(
            node.path("order_id").asInt(),
            node.path("customer_name").asText(""),
            node.path("product_name").asText(""),
            node.path("quantity").asInt(0),
            node.path("total_price").asDouble(0.0),
            node.path("payment_status").asText("Unpaid"),
            node.path("delivery_status").asText("Pending"),
            parseTimestamp(node.path("order_date").asText(null))
        );
    }

    private static Timestamp parseTimestamp(String value) {
        if (value == null || value.isBlank()) {
            return new Timestamp(System.currentTimeMillis());
        }

        try {
            Instant instant = OffsetDateTime.parse(value).toInstant();
            return Timestamp.from(instant);
        } catch (DateTimeParseException ignored) {
        }

        try {
            LocalDateTime localDateTime = LocalDateTime.parse(value.replace(' ', 'T'));
            return Timestamp.from(localDateTime.toInstant(ZoneOffset.UTC));
        } catch (DateTimeParseException ignored) {
        }

        return new Timestamp(System.currentTimeMillis());
    }

    public int getOrderId() {
        return orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public Timestamp getOrderDate() {
        return orderDate;
    }
}

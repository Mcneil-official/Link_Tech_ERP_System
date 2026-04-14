package erp.link_tech_erp.finance.repository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import erp.link_tech_erp.finance.model.Payment;
import erp.link_tech_erp.finance.model.PaymentType;

public class PaymentRepository {
    private final SupabaseRestClient restClient;
    private final ObjectMapper objectMapper;

    public PaymentRepository(SupabaseRestConfig config) {
        this.restClient = new SupabaseRestClient(config);
        this.objectMapper = new ObjectMapper();
    }

    public Payment create(Payment payment) {
        if (payment.getId() == null || payment.getId().isBlank()) {
            payment.setId(UUID.randomUUID().toString());
        }
        String body = toPaymentJson(payment);
        String response = restClient.post("/payments", body, true);
        List<Payment> parsed = parsePayments(response);
        return parsed.isEmpty() ? payment : parsed.get(0);
    }

    public List<Payment> findAll() {
        String select = URLEncoder.encode("id,payment_no,payment_type,payment_date,amount,method,reference_no,invoice_id,bill_id,notes", StandardCharsets.UTF_8);
        String response = restClient.get("/payments?select=" + select + "&order=payment_date.desc");
        return parsePayments(response);
    }

    private List<Payment> parsePayments(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            List<Payment> result = new ArrayList<>();
            if (root.isArray()) {
                for (JsonNode node : root) {
                    result.add(mapPayment(node));
                }
            }
            return result;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to parse payments response.", exception);
        }
    }

    private Payment mapPayment(JsonNode node) {
        Payment payment = new Payment();
        payment.setId(text(node, "id"));
        payment.setPaymentNo(text(node, "payment_no"));

        String paymentType = text(node, "payment_type");
        if (paymentType != null && !paymentType.isBlank()) {
            payment.setPaymentType(PaymentType.valueOf(paymentType.toUpperCase()));
        }

        String paymentDate = text(node, "payment_date");
        if (paymentDate != null && !paymentDate.isBlank()) {
            payment.setPaymentDate(LocalDate.parse(paymentDate));
        }

        payment.setAmount(number(node, "amount"));
        payment.setMethod(text(node, "method"));
        payment.setReferenceNo(text(node, "reference_no"));
        payment.setInvoiceId(text(node, "invoice_id"));
        payment.setBillId(text(node, "bill_id"));
        payment.setNotes(text(node, "notes"));
        return payment;
    }

    private String toPaymentJson(Payment payment) {
        String invoiceIdJson = payment.getInvoiceId() == null || payment.getInvoiceId().isBlank()
                ? "null"
                : "\"" + esc(payment.getInvoiceId()) + "\"";
        String billIdJson = payment.getBillId() == null || payment.getBillId().isBlank()
                ? "null"
                : "\"" + esc(payment.getBillId()) + "\"";

        return "{" +
                "\"id\":\"" + esc(payment.getId()) + "\"," +
                "\"payment_no\":\"" + esc(orEmpty(payment.getPaymentNo())) + "\"," +
                "\"payment_type\":\"" + payment.getPaymentType().name() + "\"," +
                "\"payment_date\":\"" + payment.getPaymentDate() + "\"," +
                "\"amount\":" + payment.getAmount() + "," +
                "\"method\":\"" + esc(orEmpty(payment.getMethod())) + "\"," +
                "\"reference_no\":\"" + esc(orEmpty(payment.getReferenceNo())) + "\"," +
                "\"invoice_id\":" + invoiceIdJson + "," +
                "\"bill_id\":" + billIdJson + "," +
                "\"notes\":\"" + esc(orEmpty(payment.getNotes())) + "\"" +
                "}";
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private double number(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? 0.0 : value.asDouble();
    }

    private String orEmpty(String value) {
        return value == null ? "" : value;
    }

    private String esc(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

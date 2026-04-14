package erp.link_tech_erp.finance.repository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import erp.link_tech_erp.finance.model.Bill;
import erp.link_tech_erp.finance.model.DocumentStatus;

public class BillRepository {
    private final SupabaseRestClient restClient;
    private final ObjectMapper objectMapper;

    public BillRepository(SupabaseRestConfig config) {
        this.restClient = new SupabaseRestClient(config);
        this.objectMapper = new ObjectMapper();
    }

    public Bill create(Bill bill) {
        if (bill.getId() == null || bill.getId().isBlank()) {
            bill.setId(UUID.randomUUID().toString());
        }
        if (bill.getStatus() == null) {
            bill.setStatus(DocumentStatus.DRAFT);
        }

        String body = toBillJson(bill);
        String response = restClient.post("/bills", body, true);
        List<Bill> parsed = parseBills(response);
        return parsed.isEmpty() ? bill : parsed.get(0);
    }

    public List<Bill> findAll() {
        String select = URLEncoder.encode("id,bill_no,vendor_id,issue_date,due_date,status,currency,subtotal,tax_amount,total_amount,paid_amount,notes", StandardCharsets.UTF_8);
        String response = restClient.get("/bills?select=" + select + "&order=issue_date.desc");
        return parseBills(response);
    }

    public Optional<Bill> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        String select = URLEncoder.encode("id,bill_no,vendor_id,issue_date,due_date,status,currency,subtotal,tax_amount,total_amount,paid_amount,notes", StandardCharsets.UTF_8);
        String encodedId = URLEncoder.encode(id, StandardCharsets.UTF_8);
        String response = restClient.get("/bills?select=" + select + "&id=eq." + encodedId + "&limit=1");
        List<Bill> parsed = parseBills(response);
        return parsed.isEmpty() ? Optional.empty() : Optional.of(parsed.get(0));
    }

    public boolean updatePayment(String billId, double newPaidAmount, DocumentStatus status) {
        String encodedId = URLEncoder.encode(billId, StandardCharsets.UTF_8);
        String body = "{" +
                "\"paid_amount\":" + newPaidAmount + "," +
                "\"status\":\"" + status.name() + "\"" +
                "}";
        String response = restClient.patch("/bills?id=eq." + encodedId, body);
        return response != null && response.trim().startsWith("[") && !response.trim().equals("[]");
    }

    public double getOpenPayableTotal() {
        return findAll().stream()
                .mapToDouble(bill -> Math.max(0.0, bill.getTotalAmount() - bill.getPaidAmount()))
                .sum();
    }

    private List<Bill> parseBills(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            List<Bill> result = new ArrayList<>();
            if (root.isArray()) {
                for (JsonNode node : root) {
                    result.add(mapBill(node));
                }
            }
            return result;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to parse bills response.", exception);
        }
    }

    private Bill mapBill(JsonNode node) {
        Bill bill = new Bill();
        bill.setId(text(node, "id"));
        bill.setBillNo(text(node, "bill_no"));
        bill.setVendorId(text(node, "vendor_id"));

        String issueDate = text(node, "issue_date");
        if (issueDate != null && !issueDate.isBlank()) {
            bill.setIssueDate(LocalDate.parse(issueDate));
        }

        String dueDate = text(node, "due_date");
        if (dueDate != null && !dueDate.isBlank()) {
            bill.setDueDate(LocalDate.parse(dueDate));
        }

        String status = text(node, "status");
        if (status != null && !status.isBlank()) {
            bill.setStatus(DocumentStatus.valueOf(status.toUpperCase()));
        }

        bill.setCurrency(text(node, "currency"));
        bill.setSubtotal(number(node, "subtotal"));
        bill.setTaxAmount(number(node, "tax_amount"));
        bill.setTotalAmount(number(node, "total_amount"));
        bill.setPaidAmount(number(node, "paid_amount"));
        bill.setNotes(text(node, "notes"));
        return bill;
    }

    private String toBillJson(Bill bill) {
        return "{" +
                "\"id\":\"" + esc(bill.getId()) + "\"," +
                "\"bill_no\":\"" + esc(orEmpty(bill.getBillNo())) + "\"," +
                "\"vendor_id\":\"" + esc(orEmpty(bill.getVendorId())) + "\"," +
                "\"issue_date\":\"" + bill.getIssueDate() + "\"," +
                "\"due_date\":\"" + bill.getDueDate() + "\"," +
                "\"status\":\"" + bill.getStatus().name() + "\"," +
                "\"currency\":\"" + esc(orEmpty(bill.getCurrency())) + "\"," +
                "\"subtotal\":" + bill.getSubtotal() + "," +
                "\"tax_amount\":" + bill.getTaxAmount() + "," +
                "\"total_amount\":" + bill.getTotalAmount() + "," +
                "\"paid_amount\":" + bill.getPaidAmount() + "," +
                "\"notes\":\"" + esc(orEmpty(bill.getNotes())) + "\"" +
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

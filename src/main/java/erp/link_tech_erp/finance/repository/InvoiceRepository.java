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

import erp.link_tech_erp.finance.model.DocumentStatus;
import erp.link_tech_erp.finance.model.Invoice;

public class InvoiceRepository {
    private final SupabaseRestClient restClient;
    private final ObjectMapper objectMapper;

    public InvoiceRepository(SupabaseRestConfig config) {
        this.restClient = new SupabaseRestClient(config);
        this.objectMapper = new ObjectMapper();
    }

    public Invoice create(Invoice invoice) {
        if (invoice.getId() == null || invoice.getId().isBlank()) {
            invoice.setId(UUID.randomUUID().toString());
        }
        if (invoice.getStatus() == null) {
            invoice.setStatus(DocumentStatus.DRAFT);
        }

        String body = toInvoiceJson(invoice);
        String response = restClient.post("/invoices", body, true);
        List<Invoice> parsed = parseInvoices(response);
        return parsed.isEmpty() ? invoice : parsed.get(0);
    }

    public List<Invoice> findAll() {
        String select = URLEncoder.encode("id,invoice_no,customer_id,issue_date,due_date,status,currency,subtotal,tax_amount,total_amount,paid_amount,notes", StandardCharsets.UTF_8);
        String response = restClient.get("/invoices?select=" + select + "&order=issue_date.desc");
        return parseInvoices(response);
    }

    public Optional<Invoice> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        String select = URLEncoder.encode("id,invoice_no,customer_id,issue_date,due_date,status,currency,subtotal,tax_amount,total_amount,paid_amount,notes", StandardCharsets.UTF_8);
        String encodedId = URLEncoder.encode(id, StandardCharsets.UTF_8);
        String response = restClient.get("/invoices?select=" + select + "&id=eq." + encodedId + "&limit=1");
        List<Invoice> parsed = parseInvoices(response);
        return parsed.isEmpty() ? Optional.empty() : Optional.of(parsed.get(0));
    }

    public boolean updatePayment(String invoiceId, double newPaidAmount, DocumentStatus status) {
        String encodedId = URLEncoder.encode(invoiceId, StandardCharsets.UTF_8);
        String body = "{" +
                "\"paid_amount\":" + newPaidAmount + "," +
                "\"status\":\"" + status.name() + "\"" +
                "}";
        String response = restClient.patch("/invoices?id=eq." + encodedId, body);
        return response != null && response.trim().startsWith("[") && !response.trim().equals("[]");
    }

    public double getOpenReceivableTotal() {
        return findAll().stream()
                .mapToDouble(invoice -> Math.max(0.0, invoice.getTotalAmount() - invoice.getPaidAmount()))
                .sum();
    }

    private List<Invoice> parseInvoices(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            List<Invoice> result = new ArrayList<>();
            if (root.isArray()) {
                for (JsonNode node : root) {
                    result.add(mapInvoice(node));
                }
            }
            return result;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to parse invoices response.", exception);
        }
    }

    private Invoice mapInvoice(JsonNode node) {
        Invoice invoice = new Invoice();
        invoice.setId(text(node, "id"));
        invoice.setInvoiceNo(text(node, "invoice_no"));
        invoice.setCustomerId(text(node, "customer_id"));

        String issueDate = text(node, "issue_date");
        if (issueDate != null && !issueDate.isBlank()) {
            invoice.setIssueDate(LocalDate.parse(issueDate));
        }

        String dueDate = text(node, "due_date");
        if (dueDate != null && !dueDate.isBlank()) {
            invoice.setDueDate(LocalDate.parse(dueDate));
        }

        String status = text(node, "status");
        if (status != null && !status.isBlank()) {
            invoice.setStatus(DocumentStatus.valueOf(status.toUpperCase()));
        }

        invoice.setCurrency(text(node, "currency"));
        invoice.setSubtotal(number(node, "subtotal"));
        invoice.setTaxAmount(number(node, "tax_amount"));
        invoice.setTotalAmount(number(node, "total_amount"));
        invoice.setPaidAmount(number(node, "paid_amount"));
        invoice.setNotes(text(node, "notes"));
        return invoice;
    }

    private String toInvoiceJson(Invoice invoice) {
        return "{" +
                "\"id\":\"" + esc(invoice.getId()) + "\"," +
                "\"invoice_no\":\"" + esc(orEmpty(invoice.getInvoiceNo())) + "\"," +
                "\"customer_id\":\"" + esc(orEmpty(invoice.getCustomerId())) + "\"," +
                "\"issue_date\":\"" + invoice.getIssueDate() + "\"," +
                "\"due_date\":\"" + invoice.getDueDate() + "\"," +
                "\"status\":\"" + invoice.getStatus().name() + "\"," +
                "\"currency\":\"" + esc(orEmpty(invoice.getCurrency())) + "\"," +
                "\"subtotal\":" + invoice.getSubtotal() + "," +
                "\"tax_amount\":" + invoice.getTaxAmount() + "," +
                "\"total_amount\":" + invoice.getTotalAmount() + "," +
                "\"paid_amount\":" + invoice.getPaidAmount() + "," +
                "\"notes\":\"" + esc(orEmpty(invoice.getNotes())) + "\"" +
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

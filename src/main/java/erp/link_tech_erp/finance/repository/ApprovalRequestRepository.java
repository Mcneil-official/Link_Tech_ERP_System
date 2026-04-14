package erp.link_tech_erp.finance.repository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApprovalRequestRepository {
    private final SupabaseRestClient restClient;
    private final ObjectMapper objectMapper;

    public ApprovalRequestRepository(SupabaseRestConfig config) {
        this.restClient = new SupabaseRestClient(config);
        this.objectMapper = new ObjectMapper();
    }

    public void createPending(String requestType, String sourceId, String requestedBy, String remarks) {
        String id = UUID.randomUUID().toString();
        String body = "{" +
                "\"id\":\"" + esc(id) + "\"," +
                "\"request_type\":\"" + esc(orEmpty(requestType)) + "\"," +
                "\"source_id\":\"" + esc(orEmpty(sourceId)) + "\"," +
                "\"requested_by\":\"" + esc(orEmpty(requestedBy)) + "\"," +
                "\"status\":\"PENDING\"," +
                "\"remarks\":\"" + esc(orEmpty(remarks)) + "\"" +
                "}";
        restClient.post("/approval_requests", body, false);
    }

    public JsonNode findPending(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        String response = restClient.get(
                "/approval_requests?select=id,request_type,source_id,requested_by,status,requested_at,remarks"
                        + "&status=eq.PENDING&order=requested_at.desc&limit=" + safeLimit);
        return parseArray(response, "pending approval requests");
    }

    public boolean updateStatus(String requestId, String status, String approvedBy, String remarks) {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("Approval request id is required.");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Approval status is required.");
        }

        String encodedId = URLEncoder.encode(requestId.trim(), StandardCharsets.UTF_8);
        String approvedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String body = "{"
                + "\"status\":\"" + esc(status.trim().toUpperCase()) + "\","
                + "\"approved_by\":\"" + esc(orEmpty(approvedBy)) + "\","
                + "\"approved_at\":\"" + esc(approvedAt) + "\","
                + "\"remarks\":\"" + esc(orEmpty(remarks)) + "\""
                + "}";

        String response = restClient.patch("/approval_requests?id=eq." + encodedId, body);
        JsonNode root = parseArray(response, "approval request update");
        return root.size() > 0;
    }

    private JsonNode parseArray(String json, String context) {
        try {
            JsonNode node = objectMapper.readTree(json);
            if (!node.isArray()) {
                throw new IllegalStateException("Unexpected response payload for " + context + ".");
            }
            return node;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to parse " + context + " response.", exception);
        }
    }

    private String orEmpty(String value) {
        return value == null ? "" : value;
    }

    private String esc(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

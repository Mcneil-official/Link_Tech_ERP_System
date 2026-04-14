package erp.link_tech_erp.finance.repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AuditLogRepository {
    private final SupabaseRestClient restClient;
    private final ObjectMapper objectMapper;

    public AuditLogRepository(SupabaseRestConfig config) {
        this.restClient = new SupabaseRestClient(config);
        this.objectMapper = new ObjectMapper();
    }

    public void createLog(String entityName,
                          String entityId,
                          String action,
                          String changedBy,
                          String oldDataJson,
                          String newDataJson,
                          String reason) {
        String id = UUID.randomUUID().toString();
        String changedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        String oldData = oldDataJson == null || oldDataJson.isBlank() ? "null" : oldDataJson;
        String newData = newDataJson == null || newDataJson.isBlank() ? "null" : newDataJson;

        String body = "{" +
                "\"id\":\"" + esc(id) + "\"," +
                "\"entity_name\":\"" + esc(orEmpty(entityName)) + "\"," +
                "\"entity_id\":\"" + esc(orEmpty(entityId)) + "\"," +
                "\"action\":\"" + esc(orEmpty(action)) + "\"," +
                "\"changed_by\":\"" + esc(orEmpty(changedBy)) + "\"," +
                "\"changed_at\":\"" + esc(changedAt) + "\"," +
                "\"old_data\":" + oldData + "," +
                "\"new_data\":" + newData + "," +
                "\"reason\":\"" + esc(orEmpty(reason)) + "\"" +
                "}";

        restClient.post("/audit_logs", body, false);
    }

    public JsonNode findRecent(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 300));
        String response = restClient.get(
                "/audit_logs?select=id,entity_name,entity_id,action,changed_by,changed_at,reason"
                        + "&order=changed_at.desc&limit=" + safeLimit);
        return parseArray(response, "audit logs");
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

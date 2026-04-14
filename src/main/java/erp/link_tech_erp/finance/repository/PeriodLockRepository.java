package erp.link_tech_erp.finance.repository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PeriodLockRepository {
    private final SupabaseRestClient restClient;
    private final ObjectMapper objectMapper;

    public PeriodLockRepository(SupabaseRestConfig config) {
        this.restClient = new SupabaseRestClient(config);
        this.objectMapper = new ObjectMapper();
    }

    public boolean isDateLocked(LocalDate date) {
        if (date == null) {
            return false;
        }

        String dateText = URLEncoder.encode(date.toString(), StandardCharsets.UTF_8);
        String response = restClient.get("/period_locks?select=id&period_start=lte." + dateText
                + "&period_end=gte." + dateText + "&is_locked=eq.true&limit=1");

        try {
            JsonNode root = objectMapper.readTree(response);
            return root.isArray() && !root.isEmpty();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to parse period lock response.", exception);
        }
    }

    public JsonNode findRecent(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        String response = restClient.get(
                "/period_locks?select=id,period_start,period_end,is_locked,locked_by,locked_at"
                        + "&order=period_start.desc&limit=" + safeLimit);
        return parseArray(response, "period locks");
    }

    public boolean upsertLock(LocalDate startDate, LocalDate endDate, boolean isLocked, String lockedBy) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Period start and end dates are required.");
        }

        String encodedStart = URLEncoder.encode(startDate.toString(), StandardCharsets.UTF_8);
        String encodedEnd = URLEncoder.encode(endDate.toString(), StandardCharsets.UTF_8);
        String existingResponse = restClient.get("/period_locks?select=id"
                + "&period_start=eq." + encodedStart
                + "&period_end=eq." + encodedEnd
                + "&limit=1");
        JsonNode existing = parseArray(existingResponse, "period lock lookup");

        String lockedAtValue = isLocked
                ? "\"" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\""
                : "null";
        String body = "{"
                + "\"period_start\":\"" + startDate + "\","
                + "\"period_end\":\"" + endDate + "\","
                + "\"is_locked\":" + isLocked + ","
                + "\"locked_by\":\"" + esc(orEmpty(lockedBy)) + "\","
                + "\"locked_at\":" + lockedAtValue
                + "}";

        if (existing.isEmpty()) {
            String id = UUID.randomUUID().toString();
            String createBody = "{" + "\"id\":\"" + esc(id) + "\"," + body.substring(1);
            String created = restClient.post("/period_locks", createBody, true);
            return parseArray(created, "period lock create").size() > 0;
        }

        String existingId = existing.get(0).path("id").asText();
        String encodedId = URLEncoder.encode(existingId, StandardCharsets.UTF_8);
        String updated = restClient.patch("/period_locks?id=eq." + encodedId, body);
        return parseArray(updated, "period lock update").size() > 0;
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

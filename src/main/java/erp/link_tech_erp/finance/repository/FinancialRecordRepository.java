package erp.link_tech_erp.finance.repository;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import erp.link_tech_erp.finance.model.FinancialRecord;
import erp.link_tech_erp.finance.model.RecordType;

public class FinancialRecordRepository {
    private final String restBaseUrl;
    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public FinancialRecordRepository(SupabaseRestConfig restConfig) {
        this.restBaseUrl = restConfig.getRestBaseUrl();
        this.apiKey = restConfig.getApiKey();
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public List<FinancialRecord> findAll() {
        return findAllViaRest();
    }

    private List<FinancialRecord> findAllViaRest() {
        String select = URLEncoder.encode("id,record_date,record_type,category,description,amount", StandardCharsets.UTF_8);
        String order = URLEncoder.encode("record_date.desc,id.desc", StandardCharsets.UTF_8);
        String endpoint = restBaseUrl + "/financial_records?select=" + select + "&order=" + order;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .header("apikey", apiKey)
                .header("Authorization", "Bearer " + apiKey)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                throw new IllegalStateException("Supabase REST read failed (HTTP " + response.statusCode() + "): " + safeBodySnippet(response.body()));
            }

            JsonNode array = objectMapper.readTree(response.body());
            if (!array.isArray()) {
                throw new IllegalStateException("Supabase REST returned an unexpected response format.");
            }

            List<FinancialRecord> records = new java.util.ArrayList<>();
            for (JsonNode node : array) {
                records.add(mapRecordFromJson(node));
            }
            return records;
        } catch (IOException | InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Unable to read records from Supabase REST: " + exception.getMessage(), exception);
        }
    }

    public void insert(FinancialRecord record) {
        String endpoint = restBaseUrl + "/financial_records";
        String payload = buildRecordJson(record, true);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .header("Prefer", "return=minimal")
                .header("apikey", apiKey)
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        sendAndExpectSuccess(request, "Unable to insert record into Supabase");
    }

    public boolean update(FinancialRecord record) {
        String id = record.getId();
        if (isBlank(id)) {
            return false;
        }

        String encodedId = URLEncoder.encode(id, StandardCharsets.UTF_8);
        String endpoint = restBaseUrl + "/financial_records?id=eq." + encodedId;
        String payload = buildRecordJson(record, false);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation")
                .header("apikey", apiKey)
                .header("Authorization", "Bearer " + apiKey)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(payload))
                .build();

        String body = sendAndExpectSuccess(request, "Unable to update record in Supabase");
        return body != null && body.trim().startsWith("[") && !body.trim().equals("[]");
    }

    public boolean deleteById(String id) {
        if (isBlank(id)) {
            return false;
        }

        String encodedId = URLEncoder.encode(id, StandardCharsets.UTF_8);
        String endpoint = restBaseUrl + "/financial_records?id=eq." + encodedId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(15))
                .header("Prefer", "return=representation")
                .header("apikey", apiKey)
                .header("Authorization", "Bearer " + apiKey)
                .DELETE()
                .build();

        String body = sendAndExpectSuccess(request, "Unable to delete record from Supabase");
        return body != null && body.trim().startsWith("[") && !body.trim().equals("[]");
    }

    public Optional<FinancialRecord> findById(String id) {
        if (isBlank(id)) {
            return Optional.empty();
        }

        String select = URLEncoder.encode("id,record_date,record_type,category,description,amount", StandardCharsets.UTF_8);
        String encodedId = URLEncoder.encode(id, StandardCharsets.UTF_8);
        String endpoint = restBaseUrl + "/financial_records?select=" + select + "&id=eq." + encodedId + "&limit=1";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .header("apikey", apiKey)
                .header("Authorization", "Bearer " + apiKey)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                throw new IllegalStateException("Unable to find record in Supabase (HTTP "
                        + response.statusCode() + "): " + safeBodySnippet(response.body()));
            }

            JsonNode root = objectMapper.readTree(response.body());
            if (!root.isArray() || root.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(mapRecordFromJson(root.get(0)));
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Unable to find record in Supabase.", exception);
        }
    }

    private String sendAndExpectSuccess(HttpRequest request, String errorPrefix) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                throw new IllegalStateException(errorPrefix + " (HTTP " + response.statusCode() + "): "
                        + safeBodySnippet(response.body()));
            }
            return response.body();
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException(errorPrefix + ".", exception);
        }
    }

    private String buildRecordJson(FinancialRecord record, boolean includeId) {
        StringBuilder json = new StringBuilder("{");
        if (includeId) {
            json.append("\"id\":\"").append(escapeJson(record.getId())).append("\",");
        }
        json.append("\"record_date\":\"").append(record.getDate()).append("\",")
                .append("\"record_type\":\"").append(record.getType().name()).append("\",")
                .append("\"category\":\"").append(escapeJson(nullToEmpty(record.getCategory()))).append("\",")
                .append("\"description\":\"").append(escapeJson(nullToEmpty(record.getDescription()))).append("\",")
                .append("\"amount\":").append(record.getAmount())
                .append("}");
        return json.toString();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String safeBodySnippet(String body) {
        if (body == null || body.isBlank()) {
            return "<empty response>";
        }
        return body.length() > 300 ? body.substring(0, 300) + "..." : body;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private FinancialRecord mapRecordFromJson(JsonNode node) {
        FinancialRecord record = new FinancialRecord();
        record.setId(text(node, "id"));

        String dateText = text(node, "record_date");
        if (!isBlank(dateText)) {
            record.setDate(LocalDate.parse(dateText));
        }

        String type = text(node, "record_type");
        if (!isBlank(type)) {
            record.setType(RecordType.valueOf(type.toUpperCase()));
        }

        record.setCategory(text(node, "category"));
        record.setDescription(text(node, "description"));
        JsonNode amount = node.get("amount");
        if (amount != null && !amount.isNull()) {
            record.setAmount(amount.asDouble());
        }
        return record;
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }
}

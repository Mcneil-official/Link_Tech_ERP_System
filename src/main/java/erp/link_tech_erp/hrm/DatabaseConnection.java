package erp.link_tech_erp.hrm;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import erp.link_tech_erp.integration.HrmFinanceSalarySyncService;

public class DatabaseConnection {

    private static final String TABLE = "employees";
    private static final String SELECT_FIELDS = "id,name,position,department,salary";
    private static final Map<String, String> ENV_FILE_VALUES = loadEnvFileValues();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static boolean restBaseUrlLogged;

    public static ArrayList<String[]> loadData() {
        ArrayList<String[]> list = new ArrayList<>();
        try {

            JsonNode root = get("/" + TABLE + "?select=" + SELECT_FIELDS + "&order=id.asc");
            if (root.isArray()) {
                for (JsonNode node : root) {
                    list.add(new String[] {
                        node.path("id").asText(""),
                        node.path("name").asText(""),
                        node.path("position").asText(""),
                        node.path("department").asText(""),
                        node.path("salary").asText("")
                    });
                }
            }

        } catch (Exception e) {
            System.err.println("Failed to load employee data: " + e.getMessage());
        }
        return list;
    }

    public static boolean createEmployee(String id, String name, String position, String department, String salary) {
        if (isBlank(id) || isBlank(name)) {
            return false;
        }

        try {
            if (getEmployeeById(id) != null) {
                return false;
            }

            Map<String, String> row = new HashMap<>();
            row.put("id", safe(id));
            row.put("name", safe(name));
            row.put("position", safe(position));
            row.put("department", safe(department));
            row.put("salary", safe(salary));

            String payload = OBJECT_MAPPER.writeValueAsString(row);
            post(TABLE, payload, false);
            syncSalaryToFinance(id);
            return true;
        } catch (JsonProcessingException | RuntimeException exception) {
            System.err.println("Failed to create employee: " + exception.getMessage());
            return false;
        }
    }

    public static boolean updateEmployee(String id, String name, String position, String department, String salary) {
        if (isBlank(id)) {
            return false;
        }

        try {
            Map<String, String> updates = new HashMap<>();
            updates.put("name", safe(name));
            updates.put("position", safe(position));
            updates.put("department", safe(department));
            updates.put("salary", safe(salary));

            String payload = OBJECT_MAPPER.writeValueAsString(updates);
            JsonNode response = patch("/" + TABLE + "?id=eq." + enc(id), payload, true);
            if (response.isArray() && !response.isEmpty()) {
                syncSalaryToFinance(id);
            }
            return response.isArray() && !response.isEmpty();
        } catch (JsonProcessingException | RuntimeException exception) {
            System.err.println("Failed to update employee: " + exception.getMessage());
            return false;
        }
    }

    public static boolean deleteEmployee(String id) {
        if (isBlank(id)) {
            return false;
        }

        try {
            JsonNode response = delete("/" + TABLE + "?id=eq." + enc(id), true);
            return response.isArray() && !response.isEmpty();
        } catch (Exception exception) {
            System.err.println("Failed to delete employee: " + exception.getMessage());
            return false;
        }
    }

    public static ArrayList<String[]> searchByName(String name) {
        ArrayList<String[]> list = new ArrayList<>();
        if (isBlank(name)) {
            return list;
        }

        try {
            String query = "/" + TABLE + "?select=" + SELECT_FIELDS + "&name=ilike.*" + enc(name.trim()) + "*&order=id.asc";
            JsonNode root = get(query);
            if (root.isArray()) {
                for (JsonNode node : root) {
                    list.add(new String[] {
                        node.path("id").asText(""),
                        node.path("name").asText(""),
                        node.path("position").asText(""),
                        node.path("department").asText(""),
                        node.path("salary").asText("")
                    });
                }
            }
        } catch (Exception exception) {
            System.err.println("Failed to search employees: " + exception.getMessage());
        }

        return list;
    }

    public static String[] getEmployeeById(String id) {
        if (isBlank(id)) {
            return null;
        }

        try {
            JsonNode root = get("/" + TABLE + "?select=" + SELECT_FIELDS + "&id=eq." + enc(id) + "&limit=1");
            if (!root.isArray() || root.isEmpty()) {
                return null;
            }

            JsonNode node = root.get(0);
            return new String[] {
                node.path("id").asText(""),
                node.path("name").asText(""),
                node.path("position").asText(""),
                node.path("department").asText(""),
                node.path("salary").asText("")
            };
        } catch (Exception exception) {
            System.err.println("Failed to load employee by id: " + exception.getMessage());
            return null;
        }
    }

    public static void saveData(ArrayList<String[]> list) {
        // Legacy compatibility method. Prefer createEmployee/updateEmployee/deleteEmployee.
        try {
            List<String[]> existing = loadData();
            Map<String, String[]> existingById = new HashMap<>();
            for (String[] row : existing) {
                if (row != null && row.length >= 5 && !isBlank(row[0])) {
                    existingById.put(row[0], row);
                }
            }

            Map<String, String[]> incomingById = new HashMap<>();
            for (String[] row : list) {
                if (row == null || row.length < 5 || isBlank(row[0])) {
                    continue;
                }
                incomingById.put(row[0], row);
            }

            for (String id : existingById.keySet()) {
                if (!incomingById.containsKey(id)) {
                    deleteEmployee(id);
                }
            }

            for (Map.Entry<String, String[]> entry : incomingById.entrySet()) {
                String id = entry.getKey();
                String[] row = entry.getValue();

                if (!existingById.containsKey(id)) {
                    createEmployee(row[0], row[1], row[2], row[3], row[4]);
                } else {
                    updateEmployee(row[0], row[1], row[2], row[3], row[4]);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to save employee data: " + e.getMessage());
        }
    }

    private static JsonNode get(String pathAndQuery) {
        HttpRequest request = baseRequest(pathAndQuery)
            .header("Accept", "application/json")
            .GET()
            .build();
        String body = send(request, "Supabase GET failed");
        return parse(body, "Supabase GET returned invalid JSON");
    }

    private static JsonNode post(String table, String payload, boolean returnRepresentation) {
        HttpRequest request = baseRequest("/" + table)
            .header("Content-Type", "application/json")
            .header("Prefer", returnRepresentation ? "return=representation" : "return=minimal")
            .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
            .build();
        String body = send(request, "Supabase POST failed");
        return body == null || body.isBlank() ? OBJECT_MAPPER.createArrayNode() : parse(body, "Supabase POST returned invalid JSON");
    }

    private static JsonNode delete(String pathAndQuery, boolean returnRepresentation) {
        HttpRequest request = baseRequest(pathAndQuery)
            .header("Prefer", returnRepresentation ? "return=representation" : "return=minimal")
            .DELETE()
            .build();
        String body = send(request, "Supabase DELETE failed");
        return body == null || body.isBlank() ? OBJECT_MAPPER.createArrayNode() : parse(body, "Supabase DELETE returned invalid JSON");
    }

    private static JsonNode patch(String pathAndQuery, String payload, boolean returnRepresentation) {
        HttpRequest request = baseRequest(pathAndQuery)
            .header("Content-Type", "application/json")
            .header("Prefer", returnRepresentation ? "return=representation" : "return=minimal")
            .method("PATCH", HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
            .build();
        String body = send(request, "Supabase PATCH failed");
        return body == null || body.isBlank() ? OBJECT_MAPPER.createArrayNode() : parse(body, "Supabase PATCH returned invalid JSON");
    }

    private static HttpRequest.Builder baseRequest(String pathAndQuery) {
        String restBaseUrl = resolveRestBaseUrl();
        String apiKey = resolveApiKey();

        return HttpRequest.newBuilder()
            .uri(URI.create(restBaseUrl + pathAndQuery))
            .timeout(Duration.ofSeconds(20))
            .header("apikey", apiKey)
            .header("Authorization", "Bearer " + apiKey);
    }

    private static String send(HttpRequest request, String errorPrefix) {
        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                throw new IllegalStateException(
                    errorPrefix + " (HTTP " + response.statusCode() + "): " + safeBodySnippet(response.body())
                );
            }
            return response.body();
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException(errorPrefix + ".", exception);
        }
    }

    private static JsonNode parse(String body, String errorPrefix) {
        try {
            return OBJECT_MAPPER.readTree(body);
        } catch (IOException exception) {
            throw new IllegalStateException(errorPrefix + ": " + safeBodySnippet(body), exception);
        }
    }

    private static String resolveRestBaseUrl() {
        String projectUrl = resolveSetting("SUPABASE_URL");
        String explicitRestUrl = resolveSetting("SUPABASE_REST_URL");

        String baseUrl;
        if (!isBlank(explicitRestUrl)) {
            baseUrl = trimTrailingSlash(explicitRestUrl);
            if (!baseUrl.endsWith("/rest/v1")) {
                baseUrl = baseUrl + "/rest/v1";
            }
        } else if (!isBlank(projectUrl)) {
            baseUrl = trimTrailingSlash(projectUrl) + "/rest/v1";
        } else {
            throw new IllegalStateException(
                "Missing Supabase REST URL. Set SUPABASE_URL (or SUPABASE_REST_URL)."
            );
        }

        logResolvedRestBaseUrl(baseUrl);
        return baseUrl;
    }

    private static void logResolvedRestBaseUrl(String baseUrl) {
        if (!restBaseUrlLogged) {
            restBaseUrlLogged = true;
            System.out.println("[HRM] Supabase REST base URL: " + baseUrl);
        }
    }

    private static String resolveApiKey() {
        String serviceRole = resolveSetting("SUPABASE_SERVICE_ROLE_KEY");
        String apiKey = resolveSetting("SUPABASE_API_KEY");
        String anonKey = resolveSetting("SUPABASE_ANON_KEY");

        String resolved = !isBlank(serviceRole) ? serviceRole : !isBlank(apiKey) ? apiKey : anonKey;
        if (isBlank(resolved)) {
            throw new IllegalStateException(
                "Missing Supabase API key. Set SUPABASE_SERVICE_ROLE_KEY (or SUPABASE_API_KEY/SUPABASE_ANON_KEY)."
            );
        }
        return resolved;
    }

    private static String resolveSetting(String key) {
        String fromEnv = System.getenv(key);
        if (!isBlank(fromEnv)) {
            return normalizeValue(fromEnv);
        }

        String fromProperty = System.getProperty(key.toLowerCase().replace('_', '.'));
        if (!isBlank(fromProperty)) {
            return normalizeValue(fromProperty);
        }

        String fromFile = ENV_FILE_VALUES.get(key);
        if (!isBlank(fromFile)) {
            return normalizeValue(fromFile);
        }

        return null;
    }

    private static Map<String, String> loadEnvFileValues() {
        Map<String, String> values = new HashMap<>();
        for (Path envPath : List.of(Paths.get(".env"), Paths.get(".env.local"))) {
            if (!Files.exists(envPath)) {
                continue;
            }

            try {
                List<String> lines = Files.readAllLines(envPath);
                for (String rawLine : lines) {
                    if (rawLine == null) {
                        continue;
                    }

                    String line = rawLine.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }

                    int separator = line.indexOf('=');
                    if (separator <= 0) {
                        continue;
                    }

                    String envKey = line.substring(0, separator).trim();
                    String value = line.substring(separator + 1).trim();
                    if (!envKey.isEmpty()) {
                        values.put(envKey, value);
                    }
                }
            } catch (IOException ignored) {
                return Collections.emptyMap();
            }
        }

        return values.isEmpty() ? Collections.emptyMap() : values;
    }

    private static void syncSalaryToFinance(String employeeId) {
        try {
            HrmFinanceSalarySyncService.createDefault().syncEmployeeSalary(employeeId);
        } catch (Exception exception) {
            System.err.println("[HRM] Salary sync to Finance failed for employee " + employeeId + ": "
                    + exception.getMessage());
        }
    }

    private static String normalizeValue(String value) {
        String normalized = value.trim();
        if ((normalized.startsWith("\"") && normalized.endsWith("\""))
                || (normalized.startsWith("'") && normalized.endsWith("'"))
                || (normalized.startsWith("<") && normalized.endsWith(">"))) {
            if (normalized.length() >= 2) {
                normalized = normalized.substring(1, normalized.length() - 1).trim();
            }
        }

        if (normalized.startsWith("YOUR_") || normalized.contains("<YOUR_")) {
            return null;
        }

        return normalized;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String trimTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String safeBodySnippet(String body) {
        if (body == null || body.isBlank()) {
            return "<empty response>";
        }
        return body.length() > 350 ? body.substring(0, 350) + "..." : body;
    }
}

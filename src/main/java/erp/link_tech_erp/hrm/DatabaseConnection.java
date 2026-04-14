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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DatabaseConnection {

    private static final String TABLE = "hrm_employees";
    private static final String SELECT_FIELDS = "employee_id,name,position,department,salary";
    private static final Map<String, String> ENV_FILE_VALUES = loadEnvFileValues();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static boolean restBaseUrlLogged;

    public static ArrayList<String[]> loadData() {
        ArrayList<String[]> list = new ArrayList<>();
        try {

            JsonNode root = get("/" + TABLE + "?select=" + SELECT_FIELDS + "&order=employee_id.asc");
            if (root.isArray()) {
                for (JsonNode node : root) {
                    list.add(new String[] {
                        node.path("employee_id").asText(""),
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

    public static void saveData(ArrayList<String[]> list) {
        try {
            // Keep legacy behavior (entire dataset overwrite) while storing in Supabase.
            delete("/" + TABLE + "?employee_id=neq." + enc("__never__"), false);

            List<Map<String, String>> payloadRows = new ArrayList<>();
            for (String[] emp : list) {
                if (emp == null || emp.length < 5) {
                    continue;
                }
                Map<String, String> row = new HashMap<>();
                row.put("employee_id", safe(emp[0]));
                row.put("name", safe(emp[1]));
                row.put("position", safe(emp[2]));
                row.put("department", safe(emp[3]));
                row.put("salary", safe(emp[4]));
                payloadRows.add(row);
            }

            if (!payloadRows.isEmpty()) {
                String payload = OBJECT_MAPPER.writeValueAsString(payloadRows);
                post(TABLE, payload, false);
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
        Path envPath = Paths.get(".env.local");
        if (!Files.exists(envPath)) {
            return Collections.emptyMap();
        }

        Map<String, String> values = new HashMap<>();
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

        return values;
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

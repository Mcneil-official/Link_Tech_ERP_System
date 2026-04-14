package erp.link_tech_erp.sales;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class SalesSupabaseRestClient {

    private final String restBaseUrl;
    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SalesSupabaseRestClient(SalesSupabaseRestConfig config) {
        this.restBaseUrl = config.getRestBaseUrl();
        this.apiKey = config.getApiKey();
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public JsonNode get(String pathAndQuery) {
        HttpRequest request = baseRequest(pathAndQuery)
            .header("Accept", "application/json")
            .GET()
            .build();
        String body = send(request, "Supabase GET failed");
        return parse(body, "Supabase GET returned invalid JSON");
    }

    public JsonNode post(String table, String payload, boolean returnRepresentation) {
        HttpRequest request = baseRequest("/" + table)
            .header("Content-Type", "application/json")
            .header("Prefer", returnRepresentation ? "return=representation" : "return=minimal")
            .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
            .build();
        String body = send(request, "Supabase POST failed");
        return body == null || body.isBlank() ? objectMapper.createArrayNode() : parse(body, "Supabase POST returned invalid JSON");
    }

    public JsonNode patch(String pathAndQuery, String payload, boolean returnRepresentation) {
        HttpRequest request = baseRequest(pathAndQuery)
            .header("Content-Type", "application/json")
            .header("Prefer", returnRepresentation ? "return=representation" : "return=minimal")
            .method("PATCH", HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
            .build();
        String body = send(request, "Supabase PATCH failed");
        return body == null || body.isBlank() ? objectMapper.createArrayNode() : parse(body, "Supabase PATCH returned invalid JSON");
    }

    public JsonNode delete(String pathAndQuery, boolean returnRepresentation) {
        HttpRequest request = baseRequest(pathAndQuery)
            .header("Prefer", returnRepresentation ? "return=representation" : "return=minimal")
            .DELETE()
            .build();
        String body = send(request, "Supabase DELETE failed");
        return body == null || body.isBlank() ? objectMapper.createArrayNode() : parse(body, "Supabase DELETE returned invalid JSON");
    }

    private HttpRequest.Builder baseRequest(String pathAndQuery) {
        String endpoint = restBaseUrl + pathAndQuery;
        return HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .timeout(Duration.ofSeconds(20))
            .header("apikey", apiKey)
            .header("Authorization", "Bearer " + apiKey);
    }

    private String send(HttpRequest request, String errorPrefix) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
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

    private JsonNode parse(String body, String errorPrefix) {
        try {
            return objectMapper.readTree(body);
        } catch (IOException exception) {
            throw new IllegalStateException(errorPrefix + ": " + safeBodySnippet(body), exception);
        }
    }

    public static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public static String safeBodySnippet(String body) {
        if (body == null || body.isBlank()) {
            return "<empty response>";
        }
        return body.length() > 350 ? body.substring(0, 350) + "..." : body;
    }
}

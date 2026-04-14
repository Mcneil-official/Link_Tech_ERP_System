package erp.link_tech_erp.finance.repository;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class SupabaseRestClient {
    private final HttpClient httpClient;
    private final String restBaseUrl;
    private final String apiKey;

    public SupabaseRestClient(SupabaseRestConfig config) {
        this.httpClient = HttpClient.newHttpClient();
        this.restBaseUrl = config.getRestBaseUrl();
        this.apiKey = config.getApiKey();
    }

    public String get(String pathAndQuery) {
        HttpRequest request = baseRequest(pathAndQuery)
                .header("Accept", "application/json")
                .GET()
                .build();
        return send(request, "GET", pathAndQuery);
    }

    public String post(String pathAndQuery, String jsonBody, boolean returnRepresentation) {
        HttpRequest.Builder builder = baseRequest(pathAndQuery)
                .header("Content-Type", "application/json")
                .header("Prefer", returnRepresentation ? "return=representation" : "return=minimal")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));
        return send(builder.build(), "POST", pathAndQuery);
    }

    public String patch(String pathAndQuery, String jsonBody) {
        HttpRequest request = baseRequest(pathAndQuery)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        return send(request, "PATCH", pathAndQuery);
    }

    public String delete(String pathAndQuery) {
        HttpRequest request = baseRequest(pathAndQuery)
                .header("Prefer", "return=representation")
                .DELETE()
                .build();
        return send(request, "DELETE", pathAndQuery);
    }

    private HttpRequest.Builder baseRequest(String pathAndQuery) {
        return HttpRequest.newBuilder()
                .uri(URI.create(restBaseUrl + pathAndQuery))
                .timeout(Duration.ofSeconds(20))
                .header("apikey", apiKey)
                .header("Authorization", "Bearer " + apiKey);
    }

    private String send(HttpRequest request, String method, String pathAndQuery) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                throw new IllegalStateException("Supabase REST " + method + " failed for " + pathAndQuery
                        + " (HTTP " + response.statusCode() + "): " + snippet(response.body()));
            }
            return response.body();
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Supabase REST " + method + " failed for " + pathAndQuery + ".", exception);
        }
    }

    private String snippet(String body) {
        if (body == null || body.isBlank()) {
            return "<empty response>";
        }
        return body.length() > 280 ? body.substring(0, 280) + "..." : body;
    }
}

package erp.link_tech_erp.finance.repository;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AccountRepository {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final String restBaseUrl;
    private final String apiKey;

    public AccountRepository(SupabaseRestConfig restConfig) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.restBaseUrl = restConfig.getRestBaseUrl();
        this.apiKey = restConfig.getApiKey();
    }

    public boolean authenticate(String email, String password) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        if (normalizedEmail.isBlank() || password == null || password.isBlank()) {
            return false;
        }

        String encodedEmail = URLEncoder.encode(normalizedEmail, StandardCharsets.UTF_8);
        String endpoint = restBaseUrl + "/accounts?select=password_hash&email=eq."
                + encodedEmail + "&limit=1";

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
                throw new IllegalStateException("Unable to authenticate against Supabase (HTTP "
                        + response.statusCode() + "): " + safeBodySnippet(response.body()));
            }

            JsonNode root = objectMapper.readTree(response.body());
            if (!root.isArray() || root.isEmpty()) {
                return false;
            }

            JsonNode passwordHashNode = root.get(0).get("password_hash");
            if (passwordHashNode == null || passwordHashNode.isNull()) {
                return false;
            }

            String passwordHash = passwordHashNode.asText();
            return passwordEncoder.matches(password, passwordHash);
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Unable to authenticate against Supabase.", exception);
        }
    }

    private static String safeBodySnippet(String body) {
        if (body == null || body.isBlank()) {
            return "<empty response>";
        }
        return body.length() > 300 ? body.substring(0, 300) + "..." : body;
    }
}
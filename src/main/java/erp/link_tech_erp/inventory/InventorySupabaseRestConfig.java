package erp.link_tech_erp.inventory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InventorySupabaseRestConfig {

    private final String restBaseUrl;
    private final String apiKey;
    private static final Map<String, String> ENV_FILE_VALUES = loadEnvFileValues();

    private InventorySupabaseRestConfig(String restBaseUrl, String apiKey) {
        this.restBaseUrl = restBaseUrl;
        this.apiKey = apiKey;
    }

    public static InventorySupabaseRestConfig fromEnvironment() {
        String projectUrl = getConfig("SUPABASE_URL", "supabase.url");
        String explicitRestUrl = getConfig("SUPABASE_REST_URL", "supabase.rest.url");
        String serviceRoleKey = getConfig("SUPABASE_SERVICE_ROLE_KEY", "supabase.service.role.key");
        String apiKey = getConfig("SUPABASE_API_KEY", "supabase.api.key");
        String anonKey = getConfig("SUPABASE_ANON_KEY", "supabase.anon.key");

        String baseUrl = null;
        if (!isBlank(explicitRestUrl)) {
            baseUrl = toRestBaseUrl(explicitRestUrl);
        } else if (!isBlank(projectUrl)) {
            baseUrl = toRestBaseUrl(projectUrl);
        }

        String resolvedKey = !isBlank(serviceRoleKey)
            ? serviceRoleKey
            : !isBlank(apiKey)
            ? apiKey
            : anonKey;

        if (isBlank(baseUrl) || isBlank(resolvedKey)) {
            throw new IllegalStateException(
                "Missing Supabase REST config. Set SUPABASE_URL (or SUPABASE_REST_URL) and SUPABASE_SERVICE_ROLE_KEY (or SUPABASE_API_KEY/SUPABASE_ANON_KEY)."
            );
        }

        System.out.println("[Inventory] Supabase REST base URL: " + baseUrl);

        return new InventorySupabaseRestConfig(baseUrl, resolvedKey.trim());
    }

    private static String getConfig(String envKey, String systemPropertyKey) {
        String fromEnv = System.getenv(envKey);
        if (!isBlank(fromEnv)) {
            return normalizeValue(fromEnv);
        }

        String fromProperty = System.getProperty(systemPropertyKey);
        if (!isBlank(fromProperty)) {
            return normalizeValue(fromProperty);
        }

        String fromFile = ENV_FILE_VALUES.get(envKey);
        if (!isBlank(fromFile)) {
            return normalizeValue(fromFile);
        }

        return null;
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

    private static String toRestBaseUrl(String value) {
        String normalized = trimTrailingSlash(value.trim());
        if (normalized.endsWith("/rest/v1")) {
            return normalized;
        }
        return normalized + "/rest/v1";
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

                String key = line.substring(0, separator).trim();
                String envValue = line.substring(separator + 1).trim();
                if (!key.isEmpty()) {
                    values.put(key, envValue);
                }
            }
        } catch (IOException ignored) {
            return Collections.emptyMap();
        }

        return values;
    }

    public String getRestBaseUrl() {
        return restBaseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }
}

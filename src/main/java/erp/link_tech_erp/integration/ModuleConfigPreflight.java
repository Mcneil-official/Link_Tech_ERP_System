package erp.link_tech_erp.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ModuleConfigPreflight {

    private static final Map<String, String> ENV_FILE_VALUES = loadEnvFileValues();

    private ModuleConfigPreflight() {
    }

    public static List<String> financeIssues() {
        List<String> issues = new ArrayList<>();

        String supabaseUrl = resolveSetting("SUPABASE_URL");
        String supabaseRestUrl = resolveSetting("SUPABASE_REST_URL");
        if (isBlank(supabaseUrl) && isBlank(supabaseRestUrl)) {
            issues.add("Missing SUPABASE_URL (or SUPABASE_REST_URL)");
        }

        String serviceRole = resolveSetting("SUPABASE_SERVICE_ROLE_KEY");
        String apiKey = resolveSetting("SUPABASE_API_KEY");
        String anonKey = resolveSetting("SUPABASE_ANON_KEY");
        if (isBlank(serviceRole) && isBlank(apiKey) && isBlank(anonKey)) {
            issues.add("Missing one key: SUPABASE_SERVICE_ROLE_KEY or SUPABASE_API_KEY or SUPABASE_ANON_KEY");
        }

        return issues;
    }

    public static List<String> inventoryIssues() {
        List<String> issues = new ArrayList<>();

        String supabaseUrl = resolveSetting("SUPABASE_URL");
        String supabaseRestUrl = resolveSetting("SUPABASE_REST_URL");
        if (isBlank(supabaseUrl) && isBlank(supabaseRestUrl)) {
            issues.add("Missing SUPABASE_URL (or SUPABASE_REST_URL)");
        }

        String serviceRole = resolveSetting("SUPABASE_SERVICE_ROLE_KEY");
        String apiKey = resolveSetting("SUPABASE_API_KEY");
        String anonKey = resolveSetting("SUPABASE_ANON_KEY");
        if (isBlank(serviceRole) && isBlank(apiKey) && isBlank(anonKey)) {
            issues.add("Missing one key: SUPABASE_SERVICE_ROLE_KEY or SUPABASE_API_KEY or SUPABASE_ANON_KEY");
        }

        return issues;
    }

    public static List<String> salesIssues() {
        List<String> issues = new ArrayList<>();

        String supabaseUrl = resolveSetting("SUPABASE_URL");
        String supabaseRestUrl = resolveSetting("SUPABASE_REST_URL");
        if (isBlank(supabaseUrl) && isBlank(supabaseRestUrl)) {
            issues.add("Missing SUPABASE_URL (or SUPABASE_REST_URL)");
        }

        String serviceRole = resolveSetting("SUPABASE_SERVICE_ROLE_KEY");
        String apiKey = resolveSetting("SUPABASE_API_KEY");
        String anonKey = resolveSetting("SUPABASE_ANON_KEY");
        if (isBlank(serviceRole) && isBlank(apiKey) && isBlank(anonKey)) {
            issues.add("Missing one key: SUPABASE_SERVICE_ROLE_KEY or SUPABASE_API_KEY or SUPABASE_ANON_KEY");
        }

        return issues;
    }

    public static List<String> hrmIssues() {
        List<String> issues = new ArrayList<>();

        String supabaseUrl = resolveSetting("SUPABASE_URL");
        String supabaseRestUrl = resolveSetting("SUPABASE_REST_URL");
        if (isBlank(supabaseUrl) && isBlank(supabaseRestUrl)) {
            issues.add("Missing SUPABASE_URL (or SUPABASE_REST_URL)");
        }

        String serviceRole = resolveSetting("SUPABASE_SERVICE_ROLE_KEY");
        String apiKey = resolveSetting("SUPABASE_API_KEY");
        String anonKey = resolveSetting("SUPABASE_ANON_KEY");
        if (isBlank(serviceRole) && isBlank(apiKey) && isBlank(anonKey)) {
            issues.add("Missing one key: SUPABASE_SERVICE_ROLE_KEY or SUPABASE_API_KEY or SUPABASE_ANON_KEY");
        }

        return issues;
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

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
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

                    String key = line.substring(0, separator).trim();
                    String value = line.substring(separator + 1).trim();
                    if (!key.isEmpty()) {
                        values.put(key, value);
                    }
                }
            } catch (IOException ignored) {
                return Collections.emptyMap();
            }
        }

        return values.isEmpty() ? Collections.emptyMap() : values;
    }
}

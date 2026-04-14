package erp.link_tech_erp.inventory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Database {

    private static final String DEFAULT_HOST = "db.chiqcjhtndeamefkfcjp.supabase.co";
    private static final String DEFAULT_PORT = "5432";
    private static final String DEFAULT_DATABASE = "postgres";
    private static final String DEFAULT_USER = "postgres";
    private static final Map<String, String> ENV_FILE_VALUES = loadEnvFileValues();

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        String host = resolveSetting("SUPABASE_DB_HOST", DEFAULT_HOST);
        String port = resolveSetting("SUPABASE_DB_PORT", DEFAULT_PORT);
        String database = resolveSetting("SUPABASE_DB_NAME", DEFAULT_DATABASE);
        String user = resolveSetting("SUPABASE_DB_USER", DEFAULT_USER);
        String password = resolvePassword();
        String urlOverride = resolveSetting("SUPABASE_DB_URL", null);
        String url = (urlOverride != null && !urlOverride.trim().isEmpty())
            ? urlOverride.trim()
            : "jdbc:postgresql://" + host + ":" + port + "/" + database + "?sslmode=require";
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException exception) {
            throw new SQLException(
                "Supabase connection failed for host " + host + ":" + port
                    + ". If direct host fails, set SUPABASE_DB_URL (full jdbc URL) or use Supabase pooler host (IPv4) via SUPABASE_DB_HOST and SUPABASE_DB_PORT.",
                exception
            );
        }
    }

    private static String resolveSetting(String key, String defaultValue) {
        String fromEnv = System.getenv(key);
        if (fromEnv != null && !fromEnv.trim().isEmpty()) {
            return normalizeValue(fromEnv);
        }

        String fromProperty = System.getProperty(key.toLowerCase().replace('_', '.'));
        if (fromProperty != null && !fromProperty.trim().isEmpty()) {
            return normalizeValue(fromProperty);
        }

        String fromFile = ENV_FILE_VALUES.get(key);
        if (fromFile != null && !fromFile.trim().isEmpty()) {
            return normalizeValue(fromFile);
        }

        return defaultValue;
    }

    private static String resolvePassword() {
        String fromEnv = resolveSetting("SUPABASE_DB_PASSWORD", null);
        if (fromEnv != null && !fromEnv.trim().isEmpty()) {
            return fromEnv;
        }

        throw new IllegalStateException(
            "Missing Supabase DB password. Set SUPABASE_DB_PASSWORD in environment or .env.local."
        );
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
                String value = line.substring(separator + 1).trim();
                if (!key.isEmpty()) {
                    values.put(key, value);
                }
            }
        } catch (IOException ignored) {
            return Collections.emptyMap();
        }

        return values;
    }

    private static String normalizeValue(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        if ((normalized.startsWith("\"") && normalized.endsWith("\""))
            || (normalized.startsWith("'") && normalized.endsWith("'"))
            || (normalized.startsWith("<") && normalized.endsWith(">"))) {
            if (normalized.length() >= 2) {
                normalized = normalized.substring(1, normalized.length() - 1).trim();
            }
        }

        return normalized;
    }
}

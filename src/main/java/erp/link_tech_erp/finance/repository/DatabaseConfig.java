package erp.link_tech_erp.finance.repository;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class DatabaseConfig {
    private final String url;
    private final String user;
    private final String password;

    public DatabaseConfig(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public static DatabaseConfig fromEnvironment() {
        String rawUrl = getConfig("SUPABASE_DB_URL", "supabase.db.url");
        String user = getConfig("SUPABASE_DB_USER", "supabase.db.user");
        String password = getConfig("SUPABASE_DB_PASSWORD", "supabase.db.password");

        if (isBlank(rawUrl)) {
            throw new IllegalStateException("Missing Supabase DB configuration. Set SUPABASE_DB_URL.");
        }

        if (rawUrl.contains("YOUR_ROTATED_DB_PASSWORD") || rawUrl.contains("YOUR_NEW_DB_PASSWORD")) {
            throw new IllegalStateException("SUPABASE_DB_URL still contains a placeholder password. Replace it with your actual rotated database password.");
        }

        String jdbcUrl = rawUrl;
        
        // If URL is already in full JDBC format
        if (rawUrl.startsWith("jdbc:postgresql://")) {
            // Extract credentials from query params (highest priority)
            DatabaseCredentials credentialsFromQuery = extractCredentialsFromQueryParams(rawUrl);
            if (!isBlank(credentialsFromQuery.user())) {
                user = credentialsFromQuery.user();
            }
            if (!isBlank(credentialsFromQuery.password())) {
                password = credentialsFromQuery.password();
            }
            
            // Also extract from userinfo (user:password@host format) - overrides env vars
            DatabaseCredentials credentialsFromUserInfo = extractCredentialsFromUrl(rawUrl);
            if (!isBlank(credentialsFromUserInfo.user())) {
                user = credentialsFromUserInfo.user();
            }
            if (!isBlank(credentialsFromUserInfo.password())) {
                password = credentialsFromUserInfo.password();
            }
            
            // Strip userinfo from URL for JDBC driver
            jdbcUrl = stripUserInfoFromJdbcUrl(rawUrl);
        } else {
            // Otherwise, parse as postgresql:// format
            DatabaseCredentials credentialsFromUrl = extractCredentialsFromUrl(rawUrl);
            if (isBlank(user)) {
                user = credentialsFromUrl.user();
            }
            if (isBlank(password)) {
                password = credentialsFromUrl.password();
            }
            jdbcUrl = normalizeJdbcUrl(rawUrl);
        }

        if (isBlank(user) || isBlank(password)) {
            throw new IllegalStateException("Missing Supabase credentials. Provide user and password in URL or SUPABASE_DB_USER/SUPABASE_DB_PASSWORD env vars.");
        }

        if (user.contains("@")) {
            throw new IllegalStateException("Invalid SUPABASE_DB_USER. Use the database user (for pooler: postgres.<project-ref>), not your Supabase account email.");
        }

        return new DatabaseConfig(jdbcUrl, user.trim(), password);
    }

    private static String getConfig(String envKey, String systemPropertyKey) {
        String fromEnv = System.getenv(envKey);
        if (!isBlank(fromEnv)) {
            return fromEnv;
        }
        return System.getProperty(systemPropertyKey);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String normalizeJdbcUrl(String rawUrl) {
        URI uri = toDatabaseUri(rawUrl);
        if (isBlank(uri.getHost())) {
            throw new IllegalStateException("Invalid SUPABASE_DB_URL format. Host is missing.");
        }

        StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://").append(uri.getHost());
        if (uri.getPort() > 0) {
            jdbcUrl.append(":").append(uri.getPort());
        }

        String path = uri.getRawPath();
        if (isBlank(path)) {
            path = "/postgres";
        }
        jdbcUrl.append(path);

        String query = uri.getRawQuery();
        if (!isBlank(query)) {
            jdbcUrl.append("?").append(query);
        }

        return ensureSslModeRequire(jdbcUrl.toString());
    }

    private static String ensureSslModeRequire(String jdbcUrl) {
        if (jdbcUrl.contains("sslmode=")) {
            return jdbcUrl;
        }
        return jdbcUrl + (jdbcUrl.contains("?") ? "&" : "?") + "sslmode=require";
    }

    private static String stripUserInfoFromJdbcUrl(String jdbcUrl) {
        // Remove userinfo (user:password@) from JDBC URL for security
        // Input:  jdbc:postgresql://user:password@host:port/db?params
        // Output: jdbc:postgresql://host:port/db?params
        try {
            int startIdx = jdbcUrl.indexOf("://") + 3;
            int atIdx = jdbcUrl.indexOf('@', startIdx);
            if (atIdx > startIdx) {
                // Remove everything between :// and @
                return jdbcUrl.substring(0, startIdx) + jdbcUrl.substring(atIdx + 1);
            }
            return jdbcUrl;
        } catch (Exception e) {
            return jdbcUrl; // Return as-is if parsing fails
        }
    }

    private static DatabaseCredentials extractCredentialsFromUrl(String rawUrl) {
        try {
            URI uri = toDatabaseUri(rawUrl);
            String userInfo = uri.getRawUserInfo();
            if (isBlank(userInfo)) {
                return new DatabaseCredentials(null, null);
            }

            String[] tokens = userInfo.split(":", 2);
            String user = decode(tokens[0]);
            String password = tokens.length > 1 ? decode(tokens[1]) : null;
            return new DatabaseCredentials(user, password);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("SUPABASE_DB_URL is not a valid PostgreSQL URL.", exception);
        }
    }

    private static DatabaseCredentials extractCredentialsFromQueryParams(String jdbcUrl) {
        try {
            String query = null;
            int questionIndex = jdbcUrl.indexOf('?');
            if (questionIndex > 0) {
                query = jdbcUrl.substring(questionIndex + 1);
            }
            
            if (isBlank(query)) {
                return new DatabaseCredentials(null, null);
            }

            String user = null;
            String password = null;

            for (String param : query.split("&")) {
                String[] pair = param.split("=", 2);
                if (pair.length == 2) {
                    if ("user".equals(pair[0])) {
                        user = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                    } else if ("password".equals(pair[0])) {
                        password = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                    }
                }
            }

            return new DatabaseCredentials(user, password);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to extract credentials from SUPABASE_DB_URL query parameters.", exception);
        }
    }

    private static URI toDatabaseUri(String rawUrl) {
        String uriText = rawUrl.trim();
        if (uriText.startsWith("jdbc:")) {
            uriText = uriText.substring(5);
        }
        if (!uriText.startsWith("postgresql://")) {
            throw new IllegalStateException("Invalid SUPABASE_DB_URL format. Use jdbc:postgresql://... or postgresql://...");
        }
        return URI.create(uriText);
    }

    private static String decode(String value) {
        // URLDecoder treats '+' as space; preserve literal '+' in DB credentials.
        return URLDecoder.decode(value.replace("+", "%2B"), StandardCharsets.UTF_8);
    }

    private record DatabaseCredentials(String user, String password) {
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
package com.gridwar.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts Railway / Heroku style {@code DATABASE_URL} ({@code postgresql://user:pass@host:port/db})
 * into {@code spring.datasource.*} so the app does not fall back to {@code localhost}.
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(DatabaseUrlEnvironmentPostProcessor.class);
    private static final String PROP_SOURCE = "gridwarParsedDatabaseUrl";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.getPropertySources().contains(PROP_SOURCE)) {
            return;
        }
        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return;
        }
        databaseUrl = databaseUrl.trim();
        if (databaseUrl.startsWith("jdbc:")) {
            return;
        }

        try {
            Parsed pg = parsePostgresConnectionUri(databaseUrl);
            Map<String, Object> map = new HashMap<>();
            map.put("spring.datasource.url", pg.jdbcUrl());
            map.put("spring.datasource.username", pg.username());
            map.put("spring.datasource.password", pg.password());
            environment.getPropertySources().addFirst(new MapPropertySource(PROP_SOURCE, map));
        } catch (Exception e) {
            log.warn(
                    "DATABASE_URL is set but could not be parsed; using PG*/DB_* or localhost. {}",
                    e.getMessage());
        }
    }

    private record Parsed(String jdbcUrl, String username, String password) {}

    private static Parsed parsePostgresConnectionUri(String databaseUrl) {
        URI uri = URI.create(databaseUrl);
        String scheme = uri.getScheme();
        if (scheme == null || !scheme.startsWith("postgres")) {
            throw new IllegalArgumentException("Not a postgres URL");
        }

        String rawUserInfo = uri.getRawUserInfo();
        String username = "postgres";
        String password = "";
        if (rawUserInfo != null && !rawUserInfo.isEmpty()) {
            int colon = rawUserInfo.indexOf(':');
            if (colon >= 0) {
                username = urlDecode(rawUserInfo.substring(0, colon));
                password = urlDecode(rawUserInfo.substring(colon + 1));
            } else {
                username = urlDecode(rawUserInfo);
            }
        }

        String host = uri.getHost();
        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException("Missing host");
        }
        int port = uri.getPort();
        if (port == -1) {
            port = 5432;
        }
        String path = uri.getPath();
        if (path == null || path.length() <= 1) {
            throw new IllegalArgumentException("Missing database name");
        }
        String dbName = path.substring(1);

        String rawQuery = uri.getRawQuery();
        String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
        if (rawQuery != null && !rawQuery.isEmpty()) {
            jdbcUrl += "?" + rawQuery;
        } else if (!isLocalHost(host)) {
            jdbcUrl += "?sslmode=require";
        }

        return new Parsed(jdbcUrl, username, password);
    }

    private static boolean isLocalHost(String host) {
        return "localhost".equalsIgnoreCase(host)
                || "127.0.0.1".equals(host)
                || "::1".equals(host);
    }

    private static String urlDecode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }
}

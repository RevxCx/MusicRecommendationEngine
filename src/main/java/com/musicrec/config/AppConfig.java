
package com.musicrec.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class AppConfig {

    private static final Properties PROPERTIES = new Properties();

    private static final Path CONFIG_PATH =
            Path.of("config", "spotify.properties");

    static {
        loadConfiguration();
    }

    private AppConfig() {
    }

    private static void loadConfiguration() {
        if (Files.exists(CONFIG_PATH)) {
            try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
                PROPERTIES.load(input);
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Unable to load application configuration.", e
                );
            }
        }
    }

    public static String get(String key, String defaultValue) {
        String environmentKey = key
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .replace('.', '_')
                .toUpperCase();

        String environmentValue = System.getenv(environmentKey);

        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue.trim();
        }

        String value = PROPERTIES.getProperty(key);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value.trim();
    }

    public static String getSpotifyClientId() {
        return get("spotify.client.id", "");
    }

    public static String getSpotifyRedirectUri() {
        return get(
                "spotify.redirect.uri",
                "http://127.0.0.1:8888/callback"
        );
    }

    public static Path getDatabasePath() {
        return Path.of(
                get("database.path", "data/musicrec.db")
        ).toAbsolutePath().normalize();
    }
}

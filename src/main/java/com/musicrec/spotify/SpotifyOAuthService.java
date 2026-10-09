
package com.musicrec.spotify;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicrec.config.AppConfig;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class SpotifyOAuthService {

    private static final String CALLBACK =
            "http://127.0.0.1:8888/callback";

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public CompletableFuture<SpotifyToken> connect() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return authenticate();
            } catch (Exception e) {
                throw new RuntimeException(
                        "Spotify connection failed: " + e.getMessage(), e
                );
            }
        });
    }

    private SpotifyToken authenticate() throws Exception {
        String clientId = AppConfig.getSpotifyClientId();
        String redirect = AppConfig.getSpotifyRedirectUri();

        if (clientId.isBlank()) {
            throw new IllegalStateException(
                    "Client ID missing in config/spotify.properties."
            );
        }

        if (!CALLBACK.equals(redirect)) {
            throw new IllegalStateException(
                    "Redirect URI must be " + CALLBACK
            );
        }

        String verifier = randomString(32);
        String challenge = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        MessageDigest.getInstance("SHA-256")
                                .digest(verifier.getBytes(StandardCharsets.UTF_8))
                );

        String state = randomString(32);
        CompletableFuture<Map<String, String>> callback =
                new CompletableFuture<>();

        HttpServer server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 8888), 0
        );

        server.createContext("/callback", exchange -> {
            Map<String, String> params = parseQuery(
                    exchange.getRequestURI().getRawQuery()
            );

            String page = params.containsKey("error")
                    ? "Spotify authorization was denied. You can close this tab."
                    : "Spotify authorization received. You can close this tab.";

            byte[] body = ("<html><body><h2>" + page
                    + "</h2></body></html>")
                    .getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set(
                    "Content-Type", "text/html; charset=UTF-8"
            );
            exchange.sendResponseHeaders(200, body.length);

            try (var output = exchange.getResponseBody()) {
                output.write(body);
            }

            callback.complete(params);
        });

        server.start();

        try {
            String authUrl = "https://accounts.spotify.com/authorize?"
                    + "response_type=code"
                    + "&client_id=" + encode(clientId)
                    + "&redirect_uri=" + encode(redirect)
                    + "&code_challenge_method=S256"
                    + "&code_challenge=" + encode(challenge)
                    + "&state=" + encode(state);

            if (!Desktop.isDesktopSupported()) {
                throw new IllegalStateException(
                        "Your system cannot open a browser automatically."
                );
            }

            Desktop.getDesktop().browse(URI.create(authUrl));

            Map<String, String> params =
                    callback.get(3, TimeUnit.MINUTES);

            if (!state.equals(params.get("state"))) {
                throw new SecurityException(
                        "OAuth state validation failed."
                );
            }

            if (params.containsKey("error")) {
                throw new IOException(
                        "Spotify authorization was denied."
                );
            }

            String code = params.get("code");

            if (code == null || code.isBlank()) {
                throw new IOException(
                        "No authorization code received."
                );
            }

            return exchangeCode(code, verifier, clientId, redirect);

        } finally {
            server.stop(0);
        }
    }

    private SpotifyToken exchangeCode(
            String code, String verifier,
            String clientId, String redirect
    ) throws Exception {

        String body = "client_id=" + encode(clientId)
                + "&grant_type=authorization_code"
                + "&code=" + encode(code)
                + "&redirect_uri=" + encode(redirect)
                + "&code_verifier=" + encode(verifier);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://accounts.spotify.com/api/token"))
                .header("Content-Type",
                        "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString()
        );

        JsonNode json = mapper.readTree(response.body());

        if (response.statusCode() != 200) {
            throw new IOException(
                    "Spotify token request failed (HTTP "
                            + response.statusCode() + "). Check app settings."
            );
        }

        return new SpotifyToken(
                json.path("access_token").asText(),
                json.path("refresh_token").asText(null),
                json.path("expires_in").asLong(3600)
        );
    }

    private String randomString(int bytes) {
        byte[] data = new byte[bytes];
        new SecureRandom().nextBytes(data);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(data);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();

        if (query == null || query.isBlank()) {
            return params;
        }

        for (String part : query.split("&")) {
            String[] pair = part.split("=", 2);

            String key = URLDecoder.decode(
                    pair[0], StandardCharsets.UTF_8
            );
            String value = pair.length > 1
                    ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8)
                    : "";

            params.put(key, value);
        }

        return params;
    }
}

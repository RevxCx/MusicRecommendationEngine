package com.musicrec.spotify;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicrec.config.AppConfig;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class SpotifyApiClient {
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    public List<SpotifyTrack> searchTracks(
            String query, SpotifyToken token)
            throws IOException, InterruptedException {

        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query cannot be empty.");
        }

        if (token == null) {
            throw new IllegalArgumentException("Connect Spotify first.");
        }

        refreshIfNeeded(token);

        HttpResponse<String> response = sendSearch(query, token);

        if (response.statusCode() == 401) {
            refreshAccessToken(token);
            response = sendSearch(query, token);
        }

        if (response.statusCode() != 200) {
            throw new IOException(
                    "Spotify search failed (HTTP "
                            + response.statusCode() + "): "
                            + response.body());
        }

        JsonNode items = mapper.readTree(response.body())
                .path("tracks").path("items");

        List<SpotifyTrack> tracks = new ArrayList<>();

        for (JsonNode item : items) {
            String id = text(item, "id");
            String title = text(item, "name");
            String album = text(item.path("album"), "name");

            String spotifyUrl = item.path("external_urls")
                    .path("spotify").asText("");

            List<String> artists = new ArrayList<>();

            for (JsonNode artist : item.path("artists")) {
                String name = text(artist, "name");

                if (!name.isBlank()) {
                    artists.add(name);
                }
            }

            String artworkUrl = "";
            JsonNode images = item.path("album").path("images");

            if (images.isArray() && !images.isEmpty()) {
                artworkUrl = images.get(0).path("url").asText("");
            }

            tracks.add(new SpotifyTrack(
                    id,
                    title,
                    String.join(", ", artists),
                    album,
                    spotifyUrl,
                    artworkUrl
            ));
        }

        return tracks;
    }

    private HttpResponse<String> sendSearch(
            String query, SpotifyToken token)
            throws IOException, InterruptedException {

        String encodedQuery = URLEncoder.encode(
                query, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "https://api.spotify.com/v1/search?q="
                                + encodedQuery + "&type=track&limit=10"))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization",
                        "Bearer " + token.getAccessToken())
                .header("Accept", "application/json")
                .GET()
                .build();

        return client.send(
                request, HttpResponse.BodyHandlers.ofString());
    }

    private void refreshIfNeeded(SpotifyToken token)
            throws IOException, InterruptedException {

        if (token.isExpired()) {
            refreshAccessToken(token);
        }
    }

    private void refreshAccessToken(SpotifyToken token)
            throws IOException, InterruptedException {

        if (!token.hasRefreshToken()) {
            throw new IOException(
                    "Spotify session expired. Please reconnect Spotify.");
        }

        String clientId = AppConfig.getSpotifyClientId();

        if (clientId == null || clientId.isBlank()) {
            throw new IOException("Spotify Client ID is not configured.");
        }

        String form = "grant_type=refresh_token"
                + "&refresh_token=" + encode(token.getRefreshToken())
                + "&client_id=" + encode(clientId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "https://accounts.spotify.com/api/token"))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type",
                        "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            String message;

            try {
                JsonNode error = mapper.readTree(response.body());
                message = error.path("error").asText("unknown_error");
            } catch (Exception ignored) {
                message = "unknown_error";
            }

            if ("invalid_grant".equals(message)) {
                throw new IOException(
                        "Spotify refresh token expired or was revoked. "
                                + "Reconnect Spotify.");
            }

            throw new IOException(
                    "Spotify token refresh failed (HTTP "
                            + response.statusCode() + "): " + message);
        }

        JsonNode json = mapper.readTree(response.body());

        String accessToken = json.path("access_token").asText("");
        String newRefreshToken = json.path("refresh_token").asText("");
        long expiresIn = json.path("expires_in").asLong(3600);

        if (accessToken.isBlank()) {
            throw new IOException(
                    "Spotify returned no access token during refresh.");
        }

        token.update(
                accessToken,
                newRefreshToken.isBlank() ? null : newRefreshToken,
                expiresIn
        );
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String text(JsonNode node, String field) {
        return node.path(field).asText("");
    }
}
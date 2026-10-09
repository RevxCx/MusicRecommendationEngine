package com.musicrec.spotify;

public final class SpotifyToken {
    private String accessToken;
    private String refreshToken;
    private long expiresAt;

    public SpotifyToken(String accessToken, String refreshToken, long expiresIn) {
        update(accessToken, refreshToken, expiresIn);
    }

    public synchronized void update(
            String accessToken, String refreshToken, long expiresIn) {

        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("Access token is missing.");
        }

        this.accessToken = accessToken;

        if (refreshToken != null && !refreshToken.isBlank()) {
            this.refreshToken = refreshToken;
        }

        this.expiresAt = System.currentTimeMillis() / 1000 + expiresIn;
    }

    public synchronized String getAccessToken() {
        return accessToken;
    }

    public synchronized String getRefreshToken() {
        return refreshToken;
    }

    public synchronized boolean isExpired() {
        return System.currentTimeMillis() / 1000 >= expiresAt - 60;
    }

    public synchronized boolean hasRefreshToken() {
        return refreshToken != null && !refreshToken.isBlank();
    }
}

package com.musicrec.model;

public class Artist {
    private int id;
    private final String spotifyId;
    private final String name;
    private final String genres;

    public Artist(int id, String spotifyId, String name, String genres) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Artist name cannot be empty.");
        }
        this.id = id;
        this.spotifyId = spotifyId;
        this.name = name.trim();
        this.genres = genres;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getSpotifyId() { return spotifyId; }
    public String getName() { return name; }
    public String getGenres() { return genres; }
}

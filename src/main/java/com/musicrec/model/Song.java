
package com.musicrec.model;

public class Song {
    private int id;
    private final String spotifyId;
    private final String title;
    private final int artistId;
    private final String albumName;
    private final String genre;
    private final String mood;
    private final String language;
    private final Double energy;
    private final Double tempo;
    private final Double danceability;
    private final Double valence;
    private final Integer popularity;
    private final String spotifyUrl;
    private final String artworkUrl;

    public Song(int id, String spotifyId, String title, int artistId,
                String albumName, String genre, String mood, String language,
                Double energy, Double tempo, Double danceability,
                Double valence, Integer popularity, String spotifyUrl,
                String artworkUrl) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Song title cannot be empty.");
        }
        this.id = id;
        this.spotifyId = spotifyId;
        this.title = title.trim();
        this.artistId = artistId;
        this.albumName = albumName;
        this.genre = genre;
        this.mood = mood;
        this.language = language;
        this.energy = energy;
        this.tempo = tempo;
        this.danceability = danceability;
        this.valence = valence;
        this.popularity = popularity;
        this.spotifyUrl = spotifyUrl;
        this.artworkUrl = artworkUrl;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getSpotifyId() { return spotifyId; }
    public String getTitle() { return title; }
    public int getArtistId() { return artistId; }
    public String getAlbumName() { return albumName; }
    public String getGenre() { return genre; }
    public String getMood() { return mood; }
    public String getLanguage() { return language; }
    public Double getEnergy() { return energy; }
    public Double getTempo() { return tempo; }
    public Double getDanceability() { return danceability; }
    public Double getValence() { return valence; }
    public Integer getPopularity() { return popularity; }
    public String getSpotifyUrl() { return spotifyUrl; }
    public String getArtworkUrl() { return artworkUrl; }
}

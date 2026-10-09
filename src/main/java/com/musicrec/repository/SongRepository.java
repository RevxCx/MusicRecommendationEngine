
package com.musicrec.repository;

import com.musicrec.database.DatabaseManager;
import com.musicrec.model.Artist;
import com.musicrec.model.Song;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SongRepository {

    public Artist saveArtist(Artist artist) throws SQLException {
        String sql = """
                INSERT INTO artists (spotify_id, name, genres)
                VALUES (?, ?, ?)
                ON CONFLICT(spotify_id) DO UPDATE SET
                    name=excluded.name, genres=excluded.genres
                """;

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, artist.getSpotifyId());
            ps.setString(2, artist.getName());
            ps.setString(3, artist.getGenres());
            ps.executeUpdate();

            String query = artist.getSpotifyId() == null
                    ? "SELECT id FROM artists WHERE name=?"
                    : "SELECT id FROM artists WHERE spotify_id=?";

            try (PreparedStatement find = con.prepareStatement(query)) {
                find.setString(1, artist.getSpotifyId() == null
                        ? artist.getName() : artist.getSpotifyId());

                try (ResultSet rs = find.executeQuery()) {
                    if (rs.next()) artist.setId(rs.getInt("id"));
                }
            }
        }
        return artist;
    }

    public Song saveSong(Song song) throws SQLException {
        String sql = """
                INSERT INTO songs
                (spotify_id, title, artist_id, album_name, genre, mood,
                 language, energy, tempo, danceability, valence,
                 popularity, spotify_url, artwork_url)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(spotify_id) DO UPDATE SET
                    title=excluded.title,
                    artist_id=excluded.artist_id,
                    album_name=excluded.album_name,
                    genre=excluded.genre,
                    mood=excluded.mood,
                    language=excluded.language,
                    energy=excluded.energy,
                    tempo=excluded.tempo,
                    danceability=excluded.danceability,
                    valence=excluded.valence,
                    popularity=excluded.popularity,
                    spotify_url=excluded.spotify_url,
                    artwork_url=excluded.artwork_url
                """;

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, song.getSpotifyId());
            ps.setString(2, song.getTitle());
            if (song.getArtistId() > 0) ps.setInt(3, song.getArtistId());
            else ps.setNull(3, Types.INTEGER);
            ps.setString(4, song.getAlbumName());
            ps.setString(5, song.getGenre());
            ps.setString(6, song.getMood());
            ps.setString(7, song.getLanguage());
            setDouble(ps, 8, song.getEnergy());
            setDouble(ps, 9, song.getTempo());
            setDouble(ps, 10, song.getDanceability());
            setDouble(ps, 11, song.getValence());

            if (song.getPopularity() == null) ps.setNull(12, Types.INTEGER);
            else ps.setInt(12, song.getPopularity());

            ps.setString(13, song.getSpotifyUrl());
            ps.setString(14, song.getArtworkUrl());
            ps.executeUpdate();

            String query = song.getSpotifyId() == null
                    ? "SELECT id FROM songs WHERE title=? AND artist_id=?"
                    : "SELECT id FROM songs WHERE spotify_id=?";

            try (PreparedStatement find = con.prepareStatement(query)) {
                find.setString(1, song.getSpotifyId() == null
                        ? song.getTitle() : song.getSpotifyId());
                if (song.getSpotifyId() == null) {
                    find.setInt(2, song.getArtistId());
                }
                try (ResultSet rs = find.executeQuery()) {
                    if (rs.next()) song.setId(rs.getInt("id"));
                }
            }
        }
        return song;
    }

    private void setDouble(PreparedStatement ps, int index, Double value)
            throws SQLException {
        if (value == null) ps.setNull(index, Types.REAL);
        else ps.setDouble(index, value);
    }

    public List<Song> searchByTitle(String title) throws SQLException {
        List<Song> songs = new ArrayList<>();
        String sql = """
                SELECT * FROM songs
                WHERE LOWER(title) LIKE LOWER(?)
                ORDER BY title
                """;

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + title.trim() + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) songs.add(mapSong(rs));
            }
        }
        return songs;
    }

    public List<Song> findAll() throws SQLException {
        List<Song> songs = new ArrayList<>();

        try (Connection con = DatabaseManager.getConnection();
             Statement s = con.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM songs ORDER BY title")) {
            while (rs.next()) songs.add(mapSong(rs));
        }
        return songs;
    }

    public Optional<Song> findById(int id) throws SQLException {
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM songs WHERE id=?")) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapSong(rs)) : Optional.empty();
            }
        }
    }

    private Song mapSong(ResultSet rs) throws SQLException {
        int artistId = rs.getInt("artist_id");
        if (rs.wasNull()) artistId = 0;

        return new Song(
                rs.getInt("id"),
                rs.getString("spotify_id"),
                rs.getString("title"),
                artistId,
                rs.getString("album_name"),
                rs.getString("genre"),
                rs.getString("mood"),
                rs.getString("language"),
                nullableDouble(rs, "energy"),
                nullableDouble(rs, "tempo"),
                nullableDouble(rs, "danceability"),
                nullableDouble(rs, "valence"),
                nullableInt(rs, "popularity"),
                rs.getString("spotify_url"),
                rs.getString("artwork_url")
        );
    }

    private Double nullableDouble(ResultSet rs, String column)
            throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    private Integer nullableInt(ResultSet rs, String column)
            throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }
}


package com.musicrec.repository;

import com.musicrec.database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavoriteRepository {

    public void add(int userId, int songId) throws SQLException {
        String sql = """
                INSERT OR IGNORE INTO favorites (user_id, song_id)
                VALUES (?, ?)
                """;

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, songId);
            ps.executeUpdate();
        }
    }

    public void remove(int userId, int songId) throws SQLException {
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "DELETE FROM favorites WHERE user_id=? AND song_id=?")) {
            ps.setInt(1, userId);
            ps.setInt(2, songId);
            ps.executeUpdate();
        }
    }

    public List<Integer> findSongIds(int userId) throws SQLException {
        List<Integer> ids = new ArrayList<>();

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT song_id FROM favorites WHERE user_id=? ORDER BY created_at DESC")) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt("song_id"));
            }
        }
        return ids;
    }

    public boolean isFavorite(int userId, int songId) throws SQLException {
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT 1 FROM favorites WHERE user_id=? AND song_id=?")) {
            ps.setInt(1, userId);
            ps.setInt(2, songId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}

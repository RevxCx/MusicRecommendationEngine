
package com.musicrec.repository;

import com.musicrec.database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryRepository {

    public void recordListening(int userId, int songId)
            throws SQLException {
        String sql = """
                INSERT INTO listening_history (user_id, song_id)
                VALUES (?, ?)
                """;

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, songId);
            ps.executeUpdate();
        }
    }

    public List<Integer> findRecentSongIds(int userId, int limit)
            throws SQLException {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive.");
        }

        List<Integer> ids = new ArrayList<>();
        String sql = """
                SELECT song_id FROM listening_history
                WHERE user_id=?
                ORDER BY listened_at DESC, id DESC
                LIMIT ?
                """;

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt("song_id"));
            }
        }
        return ids;
    }
}

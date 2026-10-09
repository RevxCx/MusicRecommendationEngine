
package com.musicrec.repository;

import com.musicrec.database.DatabaseManager;
import com.musicrec.model.User;

import java.sql.*;
import java.util.Optional;

public class UserRepository {

    public User save(User user) throws SQLException {
        String sql = """
                INSERT INTO users
                (name, preferred_genre, preferred_mood,
                 preferred_language, preferred_energy)
                VALUES (?, ?, ?, ?, ?)
                """;

        if (user.getId() > 0) {
            sql = """
                    UPDATE users SET name=?, preferred_genre=?,
                    preferred_mood=?, preferred_language=?,
                    preferred_energy=? WHERE id=?
                    """;
        }

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getPreferredGenre());
            ps.setString(3, user.getPreferredMood());
            ps.setString(4, user.getPreferredLanguage());
            ps.setString(5, user.getPreferredEnergy());

            if (user.getId() > 0) {
                ps.setInt(6, user.getId());
                if (ps.executeUpdate() == 0) {
                    throw new SQLException("User not found: " + user.getId());
                }
            } else {
                ps.executeUpdate();
                try (Statement s = con.createStatement();
                     ResultSet rs = s.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) user.setId(rs.getInt(1));
                }
            }
        }
        return user;
    }

    public Optional<User> findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id=?";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                return Optional.of(new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("preferred_genre"),
                        rs.getString("preferred_mood"),
                        rs.getString("preferred_language"),
                        rs.getString("preferred_energy")
                ));
            }
        }
    }
}

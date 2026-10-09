package com.musicrec.repository;

import com.musicrec.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class FeedbackRepository {

    public void setRating(int userId, String trackTitle, int rating)
            throws SQLException {

        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID.");
        }

        if (trackTitle == null || trackTitle.isBlank()) {
            throw new IllegalArgumentException("Track title is required.");
        }

        if (rating != -1 && rating != 0 && rating != 1) {
            throw new IllegalArgumentException(
                    "Rating must be -1, 0, or 1.");
        }

        if (rating == 0) {
            try (Connection connection = DatabaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement("""
                         DELETE FROM recommendation_feedback
                         WHERE user_id = ? AND track_title = ?
                         """)) {

                statement.setInt(1, userId);
                statement.setString(2, trackTitle);
                statement.executeUpdate();
            }

            return;
        }

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO recommendation_feedback
                         (user_id, track_title, rating)
                     VALUES (?, ?, ?)
                     ON CONFLICT(user_id, track_title)
                     DO UPDATE SET
                         rating = excluded.rating,
                         updated_at = CURRENT_TIMESTAMP
                     """)) {

            statement.setInt(1, userId);
            statement.setString(2, trackTitle);
            statement.setInt(3, rating);
            statement.executeUpdate();
        }
    }

    public Map<String, Integer> findRatings(int userId)
            throws SQLException {

        Map<String, Integer> ratings = new HashMap<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT track_title, rating
                     FROM recommendation_feedback
                     WHERE user_id = ?
                     """)) {

            statement.setInt(1, userId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    ratings.put(
                            result.getString("track_title"),
                            result.getInt("rating")
                    );
                }
            }
        }

        return ratings;
    }
}
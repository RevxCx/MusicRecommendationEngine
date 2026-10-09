package com.musicrec.database;

import com.musicrec.config.AppConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager {

    private static final Path DATABASE_PATH =
            AppConfig.getDatabasePath();

    private static final String JDBC_URL =
            "jdbc:sqlite:" + DATABASE_PATH;

    private DatabaseManager() {
    }

    public static void initialize() {
        try {
            Path parent = DATABASE_PATH.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (Connection connection = getConnection();
                 Statement statement = connection.createStatement()) {

                statement.execute("PRAGMA journal_mode = WAL");
                statement.execute("PRAGMA foreign_keys = ON");
                createTables(statement);
            }

        } catch (IOException | SQLException e) {
            throw new IllegalStateException(
                    "Database initialization failed.", e
            );
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(JDBC_URL);

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 5000");
        } catch (SQLException e) {
            connection.close();
            throw e;
        }

        return connection;
    }

    private static void createTables(Statement statement)
            throws SQLException {

        statement.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    preferred_genre TEXT,
                    preferred_mood TEXT,
                    preferred_language TEXT,
                    preferred_energy TEXT,
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS artists (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    spotify_id TEXT UNIQUE,
                    name TEXT NOT NULL,
                    genres TEXT
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS songs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    spotify_id TEXT UNIQUE,
                    title TEXT NOT NULL,
                    artist_id INTEGER,
                    album_name TEXT,
                    genre TEXT,
                    mood TEXT,
                    language TEXT,
                    energy REAL,
                    tempo REAL,
                    danceability REAL,
                    valence REAL,
                    popularity INTEGER,
                    spotify_url TEXT,
                    artwork_url TEXT,
                    FOREIGN KEY (artist_id)
                        REFERENCES artists(id)
                        ON DELETE SET NULL
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS favorites (
                    user_id INTEGER NOT NULL,
                    song_id INTEGER NOT NULL,
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (user_id, song_id),
                    FOREIGN KEY (user_id)
                        REFERENCES users(id)
                        ON DELETE CASCADE,
                    FOREIGN KEY (song_id)
                        REFERENCES songs(id)
                        ON DELETE CASCADE
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS listening_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    song_id INTEGER NOT NULL,
                    listened_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id)
                        REFERENCES users(id)
                        ON DELETE CASCADE,
                    FOREIGN KEY (song_id)
                        REFERENCES songs(id)
                        ON DELETE CASCADE
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS recommendation_feedback (
                    user_id INTEGER NOT NULL,
                    track_title TEXT NOT NULL,
                    rating INTEGER NOT NULL
                        CHECK (rating IN (-1, 1)),
                    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (user_id, track_title),
                    FOREIGN KEY (user_id)
                        REFERENCES users(id)
                        ON DELETE CASCADE
                )
                """);

        statement.execute("""
                CREATE INDEX IF NOT EXISTS idx_songs_artist
                ON songs(artist_id)
                """);

        statement.execute("""
                CREATE INDEX IF NOT EXISTS idx_history_user_date
                ON listening_history(user_id, listened_at)
                """);

        statement.execute("""
                CREATE INDEX IF NOT EXISTS idx_feedback_user
                ON recommendation_feedback(user_id)
                """);
    }
}
package com.musicrec.spotify;

import com.musicrec.model.User;
import com.musicrec.repository.FeedbackRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LocalRecommendationEngine {

    public record Track(
            String title,
            String artist,
            String genre,
            String mood,
            String language,
            String energy
    ) {}

    public record Result(
            Track track,
            int score,
            String reason,
            int feedback
    ) {}

    private final FeedbackRepository feedbackRepository =
            new FeedbackRepository();

    private final List<Track> catalog = List.of(
            new Track("Midnight Drive", "Demo Artist A",
                    "Pop", "Happy", "English", "High"),
            new Track("Quiet Skies", "Demo Artist B",
                    "Indie", "Calm", "English", "Low"),
            new Track("Neon Pulse", "Demo Artist C",
                    "Electronic", "Happy", "English", "High"),
            new Track("Monsoon Memories", "Demo Artist D",
                    "Bollywood", "Relaxed", "Hindi", "Medium"),
            new Track("Golden Hour", "Demo Artist E",
                    "Pop", "Relaxed", "English", "Medium"),
            new Track("After the Rain", "Demo Artist F",
                    "Indie", "Sad", "English", "Low"),
            new Track("City Lights", "Demo Artist G",
                    "Hip-Hop", "Energetic", "English", "High"),
            new Track("Dil Se", "Demo Artist H",
                    "Bollywood", "Happy", "Hindi", "High"),
            new Track("Ocean Waves", "Demo Artist I",
                    "Ambient", "Calm", "English", "Low"),
            new Track("Sunday Morning", "Demo Artist J",
                    "Jazz", "Relaxed", "English", "Low"),
            new Track("Electric Dreams", "Demo Artist K",
                    "Electronic", "Energetic", "English", "High"),
            new Track("Raabta Nights", "Demo Artist L",
                    "Bollywood", "Romantic", "Hindi", "Medium")
    );

    public List<Result> recommend(User user) {
        if (user == null) {
            return List.of();
        }

        try {
            Map<String, Integer> ratings =
                    feedbackRepository.findRatings(user.getId());

            List<Result> results = new ArrayList<>();

            for (Track track : catalog) {
                int score = 0;
                List<String> reasons = new ArrayList<>();

                if (matches(user.getPreferredGenre(), track.genre())) {
                    score += 40;
                    reasons.add("Matches your genre");
                }

                if (matches(user.getPreferredMood(), track.mood())) {
                    score += 30;
                    reasons.add("Matches your mood");
                }

                if (matches(
                        user.getPreferredLanguage(), track.language())) {
                    score += 20;
                    reasons.add("Matches your language");
                }

                if (matches(user.getPreferredEnergy(), track.energy())) {
                    score += 10;
                    reasons.add("Matches your energy");
                }

                int feedback = ratings.getOrDefault(track.title(), 0);

                if (feedback == 1) {
                    score += 10;
                    reasons.add("You liked this track");
                } else if (feedback == -1) {
                    score -= 30;
                    reasons.add("You disliked this track");
                }

                score = Math.max(0, Math.min(100, score));

                if (reasons.isEmpty()) {
                    reasons.add("Explore something different");
                }

                results.add(new Result(
                        track,
                        score,
                        String.join(" • ", reasons),
                        feedback
                ));
            }

            results.sort(
                    Comparator.comparingInt(Result::score).reversed()
                            .thenComparing(r -> r.track().title())
            );

            return results;

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Unable to load recommendation feedback.", e);
        }
    }

    private boolean matches(String preference, String value) {
        return preference != null
                && !preference.isBlank()
                && value != null
                && preference.trim().toLowerCase(Locale.ROOT)
                        .equals(value.trim().toLowerCase(Locale.ROOT));
    }
}
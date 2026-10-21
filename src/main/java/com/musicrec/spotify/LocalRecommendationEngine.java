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
            String title, String artist, String genre,
            String mood, String language, String energy
    ) {}

    public record Result(Track track, int score, String reason, int feedback) {}

    private final FeedbackRepository feedbackRepository = new FeedbackRepository();
    private final List<Track> catalog = buildCatalog();

    private static List<Track> buildCatalog() {
        List<Track> tracks = new ArrayList<>();

        // Preserve the original twelve titles so saved feedback still applies.
        tracks.add(new Track("Midnight Drive", "Demo Artist A", "Pop", "Happy", "English", "High"));
        tracks.add(new Track("Quiet Skies", "Demo Artist B", "Indie", "Calm", "English", "Low"));
        tracks.add(new Track("Neon Pulse", "Demo Artist C", "Electronic", "Happy", "English", "High"));
        tracks.add(new Track("Monsoon Memories", "Demo Artist D", "Bollywood", "Relaxed", "Hindi", "Medium"));
        tracks.add(new Track("Golden Hour", "Demo Artist E", "Pop", "Relaxed", "English", "Medium"));
        tracks.add(new Track("After the Rain", "Demo Artist F", "Indie", "Sad", "English", "Low"));
        tracks.add(new Track("City Lights", "Demo Artist G", "Hip-Hop", "Energetic", "English", "High"));
        tracks.add(new Track("Dil Se", "Demo Artist H", "Bollywood", "Happy", "Hindi", "High"));
        tracks.add(new Track("Ocean Waves", "Demo Artist I", "Ambient", "Calm", "English", "Low"));
        tracks.add(new Track("Sunday Morning", "Demo Artist J", "Jazz", "Relaxed", "English", "Low"));
        tracks.add(new Track("Electric Dreams", "Demo Artist K", "Electronic", "Energetic", "English", "High"));
        tracks.add(new Track("Raabta Nights", "Demo Artist L", "Bollywood", "Romantic", "Hindi", "Medium"));

        String[] titles = {
            "Velvet Skies", "Paper Planes", "Blue Horizon", "Starlit Avenue",
            "Soft Focus", "Parallel Lines", "Summer Static", "Midnight Bloom",
            "Silver Lining", "Dancing in June", "Slow Motion", "Open Roads",
            "Little Wonders", "Fading Echoes", "Gravity Games", "City of Gold",
            "Dream Sequence", "Wildflower Season", "Northern Lights", "Daydream Radio",
            "Moonlit Cafe", "Heartbeat Avenue", "Cloud Nine", "Retrograde",
            "Somewhere Else", "Warm December", "New Beginnings", "Crimson Skies",
            "Paper Moon", "Better Days", "Echo Chamber", "Stolen Moments",
            "Sunset Lover", "Hidden Signals", "Runaway Train", "Blue Velvet",
            "Lunar Tides", "Golden State", "Fire Escape", "Slow Burn",
            "Wild at Heart", "Crystal Rain", "Afterglow", "Parallel Universe",
            "Distant Shore", "Sweet Escape", "Night Owl", "Daylight Dream",
            "Fever Dream", "Endless Summer", "Little by Little", "Dark Matter",
            "Bloom Again", "Last Train Home", "City of Dreams", "Falling Stars",
            "Homebound", "Paper Hearts", "Neon Skyline", "Lost in Stereo",
            "Breathe Again", "Moonflower", "Soft Landing", "Golden Days",
            "Halfway Home", "Quiet Fire", "Tidal Wave", "Better Together",
            "Midnight Radio", "Sunroom", "Electric Blue", "Drift Away",
            "Wildfire", "One More Night", "Dreamcatcher", "Sweet Nothing",
            "After Hours", "Raincheck", "First Light", "Side Streets",
            "Cosmic Love", "Slow Dancing", "Bright Side", "Hometown Lights",
            "Nocturne", "Summer Rain", "Far Away", "New Moon"
        };

        String[] artists = {
            "Demo Artist M", "Demo Artist N", "Demo Artist O", "Demo Artist P",
            "Demo Artist Q", "Demo Artist R", "Demo Artist S", "Demo Artist T",
            "Demo Artist U", "Demo Artist V", "Demo Artist W", "Demo Artist X"
        };

        String[] genres = {
            "Pop", "Indie", "Hip-Hop", "Electronic",
            "Bollywood", "Ambient", "Jazz"
        };
        String[] moods = {
            "Happy", "Calm", "Relaxed", "Sad", "Energetic", "Romantic"
        };
        String[] languages = {"English", "Hindi"};
        String[] energies = {"Low", "Medium", "High"};

        for (int i = 0; i < titles.length; i++) {
            int genreIndex = i % genres.length;
            String genre = genres[genreIndex];

            String language = genre.equals("Bollywood")
                    ? "Hindi"
                    : languages[(i / 2) % languages.length];

            String mood = moods[(i * 5 + 1) % moods.length];
            String energy = energies[(i * 2 + genreIndex) % energies.length];

            tracks.add(new Track(
                    titles[i],
                    artists[i % artists.length],
                    genre,
                    mood,
                    language,
                    energy
            ));
        }

        return List.copyOf(tracks);
    }

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

                if (matches(user.getPreferredLanguage(), track.language())) {
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
                    "Unable to load recommendation feedback.", e
            );
        }
    }

    public int getCatalogSize() {
        return catalog.size();
    }

    private boolean matches(String preference, String value) {
        return preference != null
                && !preference.isBlank()
                && value != null
                && preference.trim().toLowerCase(Locale.ROOT)
                        .equals(value.trim().toLowerCase(Locale.ROOT));
    }
}
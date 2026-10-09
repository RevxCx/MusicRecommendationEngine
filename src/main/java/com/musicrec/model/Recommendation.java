
package com.musicrec.model;

public class Recommendation {
    private final Song song;
    private final double score;
    private final String reason;

    public Recommendation(Song song, double score, String reason) {
        if (song == null) {
            throw new IllegalArgumentException("Song cannot be null.");
        }
        if (!Double.isFinite(score) || score < 0 || score > 100) {
            throw new IllegalArgumentException("Score must be between 0 and 100.");
        }
        this.song = song;
        this.score = score;
        this.reason = reason == null ? "" : reason;
    }

    public Song getSong() { return song; }
    public double getScore() { return score; }
    public String getReason() { return reason; }
}

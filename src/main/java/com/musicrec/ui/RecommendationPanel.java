package com.musicrec.ui;

import com.musicrec.model.User;
import com.musicrec.repository.FeedbackRepository;
import com.musicrec.spotify.LocalRecommendationEngine;
import com.musicrec.spotify.LocalRecommendationEngine.Result;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class RecommendationPanel extends JPanel {

    private static final int PAGE_SIZE = 6;

    private final User user;
    private final LocalRecommendationEngine engine =
            new LocalRecommendationEngine();
    private final FeedbackRepository feedbackRepository =
            new FeedbackRepository();

    private final JPanel resultsPanel = new JPanel();
    private final JLabel pageLabel = new JLabel(" ");
    private final JLabel statusLabel = new JLabel(" ");
    private final JButton discoverButton =
            new JButton("↻  Discover more");

    private int offset = 0;

    public RecommendationPanel(User user) {
        this.user = user;

        setLayout(new BorderLayout(20, 20));
        setBackground(UiStyle.BG);
        setBorder(new EmptyBorder(28, 30, 24, 30));

        // Header
        JLabel heading = UiStyle.heading("Made for you  ✦", 30);
        JLabel subtitle = UiStyle.muted(
                "Your preferences, your feedback, your next favorite."
        );

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(heading);
        titles.add(Box.createVerticalStrut(7));
        titles.add(subtitle);

        UiStyle.primaryButton(discoverButton);
        discoverButton.addActionListener(e -> showNextPage());

        JPanel header = new JPanel(new BorderLayout(15, 10));
        header.setOpaque(false);
        header.add(titles, BorderLayout.CENTER);
        header.add(discoverButton, BorderLayout.EAST);

        // Recommendation list
        resultsPanel.setOpaque(false);
        resultsPanel.setLayout(new BoxLayout(
                resultsPanel, BoxLayout.Y_AXIS
        ));

        // Footer
        statusLabel.setForeground(UiStyle.ACCENT_HOVER);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        pageLabel.setForeground(UiStyle.MUTED);
        pageLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.add(statusLabel);
        footer.add(Box.createVerticalStrut(6));
        footer.add(pageLabel);

        add(header, BorderLayout.NORTH);
        add(UiStyle.scrollPane(resultsPanel), BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        refreshRecommendations();
    }

    public void refreshRecommendations() {
        resultsPanel.removeAll();

        List<Result> results;

        try {
            results = engine.recommend(user);
        } catch (IllegalStateException e) {
            showError("Unable to load recommendations. Check your database.");
            return;
        }

        if (results.isEmpty()) {
            JLabel empty = UiStyle.heading(
                    "No recommendations available yet.", 18
            );
            empty.setBorder(new EmptyBorder(20, 5, 20, 5));
            resultsPanel.add(empty);
            pageLabel.setText("No tracks available");
        } else {
            int total = results.size();

            // Keep the page index valid if the catalog size changes.
            offset = (offset / PAGE_SIZE * PAGE_SIZE) % total;

            int end = Math.min(offset + PAGE_SIZE, total);

            for (int i = offset; i < end; i++) {
                resultsPanel.add(createTrackCard(i + 1, results.get(i)));
                resultsPanel.add(Box.createVerticalStrut(12));
            }

            pageLabel.setText(
                    "♫   Showing " + (offset + 1) + "–" + end
                            + " of " + total + " tracks"
            );
        }

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private void showNextPage() {
        try {
            List<Result> results = engine.recommend(user);

            if (results.isEmpty()) {
                statusLabel.setText("No more tracks available.");
                return;
            }

            offset = (offset + PAGE_SIZE) % results.size();
            statusLabel.setText(" ");
            refreshRecommendations();

        } catch (IllegalStateException e) {
            showError("Unable to load recommendations.");
        }
    }

    private void saveFeedback(Result result, int rating) {
        // Clicking the same rating again removes it.
        int newRating = result.feedback() == rating ? 0 : rating;

        try {
            feedbackRepository.setRating(
                    user.getId(),
                    result.track().title(),
                    newRating
            );

            statusLabel.setForeground(UiStyle.ACCENT_HOVER);
            statusLabel.setText(
                    newRating == 1 ? "♥  Added to your liked tracks"
                            : newRating == -1 ? "Feedback saved"
                            : "Feedback removed"
            );

            refreshRecommendations();

        } catch (SQLException e) {
            statusLabel.setForeground(UiStyle.RED);
            statusLabel.setText("Couldn't save feedback.");

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to save feedback:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private JPanel createTrackCard(int rank, Result result) {
        JPanel card = new JPanel(new BorderLayout(18, 0));
        card.setBackground(UiStyle.PANEL);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiStyle.BORDER),
                new EmptyBorder(14, 16, 14, 16)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // LEFT: artwork tile
        JPanel artwork = new JPanel(new BorderLayout(0, 6));
        artwork.setPreferredSize(new Dimension(94, 140));
        artwork.setBackground(
                rank % 3 == 0
                        ? new Color(64, 39, 83)
                        : rank % 3 == 1
                        ? new Color(35, 48, 76)
                        : new Color(54, 38, 72)
        );

        JLabel musicIcon = new JLabel("♫", SwingConstants.CENTER);
        musicIcon.setFont(new Font("SansSerif", Font.BOLD, 38));
        musicIcon.setForeground(UiStyle.ACCENT_HOVER);

        JLabel rankLabel = new JLabel(
                String.format("TRACK %02d", rank),
                SwingConstants.CENTER
        );
        rankLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        rankLabel.setForeground(UiStyle.MUTED);

        artwork.add(musicIcon, BorderLayout.CENTER);
        artwork.add(rankLabel, BorderLayout.SOUTH);

        // CENTER: track information
        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

        JLabel title = UiStyle.heading(result.track().title(), 17);

        JLabel artist = UiStyle.muted(
                result.track().artist() + "   •   "
                        + result.track().genre() + "   •   "
                        + result.track().language()
        );

        JLabel mood = UiStyle.muted(
                result.track().mood() + " mood   •   "
                        + result.track().energy() + " energy"
        );

        JLabel reason = new JLabel(result.reason());
        reason.setForeground(UiStyle.ACCENT_HOVER);
        reason.setFont(new Font("SansSerif", Font.PLAIN, 11));

        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        artist.setAlignmentX(Component.LEFT_ALIGNMENT);
        mood.setAlignmentX(Component.LEFT_ALIGNMENT);
        reason.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Match meter
        int score = Math.max(0, Math.min(100, result.score()));

        JProgressBar matchBar = new JProgressBar(0, 100);
        matchBar.setValue(score);
        matchBar.setStringPainted(false);
        matchBar.setForeground(UiStyle.ACCENT);
        matchBar.setBackground(UiStyle.PANEL_HOVER);
        matchBar.setBorderPainted(false);
        matchBar.setPreferredSize(new Dimension(150, 8));
        matchBar.setMinimumSize(new Dimension(100, 8));
        matchBar.setMaximumSize(new Dimension(220, 8));

        JLabel scoreText = new JLabel(score + "% match");
        scoreText.setForeground(UiStyle.TEXT);
        scoreText.setFont(new Font("SansSerif", Font.BOLD, 12));

        JPanel scoreLine = new JPanel(new FlowLayout(
                FlowLayout.LEFT, 10, 0
        ));
        scoreLine.setOpaque(false);
        scoreLine.setAlignmentX(Component.LEFT_ALIGNMENT);
        scoreLine.add(matchBar);
        scoreLine.add(scoreText);

        // LIKE button
        JButton like = new JButton(
                result.feedback() == 1 ? "♥  Liked" : "♡  Like"
        );
        UiStyle.primaryButton(like);
        like.setForeground(Color.WHITE);
        like.setFont(new Font("SansSerif", Font.BOLD, 12));
        like.setPreferredSize(new Dimension(112, 36));
        like.setMinimumSize(new Dimension(112, 36));
        like.setMaximumSize(new Dimension(112, 36));
        like.setToolTipText("Like this track");
        like.addActionListener(e -> saveFeedback(result, 1));

        // DISLIKE button
        JButton dislike = new JButton(
                result.feedback() == -1
                        ? "✓  Disliked"
                        : "✕  Not for me"
        );
        UiStyle.button(dislike);
        dislike.setForeground(UiStyle.TEXT);
        dislike.setFont(new Font("SansSerif", Font.BOLD, 12));
        dislike.setPreferredSize(new Dimension(132, 36));
        dislike.setMinimumSize(new Dimension(132, 36));
        dislike.setMaximumSize(new Dimension(132, 36));
        dislike.setToolTipText("Dislike this track");
        dislike.addActionListener(e -> saveFeedback(result, -1));

        // Dedicated button row
        JPanel actions = new JPanel(new FlowLayout(
                FlowLayout.LEFT, 8, 0
        ));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.add(like);
        actions.add(dislike);

        details.add(title);
        details.add(Box.createVerticalStrut(5));
        details.add(artist);
        details.add(Box.createVerticalStrut(4));
        details.add(mood);
        details.add(Box.createVerticalStrut(6));
        details.add(reason);
        details.add(Box.createVerticalStrut(9));
        details.add(scoreLine);
        details.add(Box.createVerticalStrut(10));
        details.add(actions);

        card.add(artwork, BorderLayout.WEST);
        card.add(details, BorderLayout.CENTER);

        // Hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(UiStyle.PANEL_HOVER);
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UiStyle.ACCENT_DARK),
                        new EmptyBorder(14, 16, 14, 16)
                ));
                card.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(UiStyle.PANEL);
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UiStyle.BORDER),
                        new EmptyBorder(14, 16, 14, 16)
                ));
                card.repaint();
            }
        });

        return card;
    }

    private void showError(String message) {
        resultsPanel.removeAll();

        JLabel error = new JLabel(message);
        error.setForeground(UiStyle.RED);
        error.setBorder(new EmptyBorder(15, 5, 15, 5));

        resultsPanel.add(error);
        statusLabel.setText(" ");

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }
}
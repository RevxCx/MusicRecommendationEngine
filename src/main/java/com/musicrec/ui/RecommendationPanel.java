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

    private static final Color BG = new Color(18, 18, 18);
    private static final Color CARD = new Color(35, 35, 35);
    private static final Color GREEN = new Color(30, 215, 96);
    private static final Color TEXT = new Color(245, 245, 245);
    private static final Color MUTED = new Color(175, 175, 175);
    private static final Color RED = new Color(255, 110, 110);

    private static final int PAGE_SIZE = 6;

    private final User user;
    private final LocalRecommendationEngine engine =
            new LocalRecommendationEngine();
    private final FeedbackRepository feedbackRepository =
            new FeedbackRepository();

    private final JPanel resultsPanel = new JPanel();
    private final JLabel pageLabel = new JLabel();
    private final JLabel statusLabel = new JLabel(" ");

    private int offset = 0;

    public RecommendationPanel(User user) {
        this.user = user;

        setLayout(new BorderLayout(15, 15));
        setBackground(BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel heading = new JLabel("Made for you");
        heading.setForeground(TEXT);
        heading.setFont(new Font("SansSerif", Font.BOLD, 28));

        JLabel subtitle = new JLabel(
                "Recommendations based on your preferences and feedback");
        subtitle.setForeground(MUTED);

        JButton refreshButton = new JButton("Discover More");
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> showNextPage());

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(heading);
        titles.add(Box.createVerticalStrut(6));
        titles.add(subtitle);

        JPanel header = new JPanel(new BorderLayout(10, 10));
        header.setOpaque(false);
        header.add(titles, BorderLayout.CENTER);
        header.add(refreshButton, BorderLayout.EAST);

        resultsPanel.setBackground(BG);
        resultsPanel.setLayout(new BoxLayout(
                resultsPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(resultsPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        pageLabel.setForeground(MUTED);
        pageLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        statusLabel.setForeground(GREEN);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.add(statusLabel);
        bottom.add(Box.createVerticalStrut(5));
        bottom.add(pageLabel);

        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        refreshRecommendations();
    }

    public void refreshRecommendations() {
        resultsPanel.removeAll();

        List<Result> results;

        try {
            results = engine.recommend(user);
        } catch (IllegalStateException e) {
            JLabel error = new JLabel(
                    "Unable to load recommendations. Check the database.");
            error.setForeground(RED);
            resultsPanel.add(error);
            resultsPanel.revalidate();
            resultsPanel.repaint();
            return;
        }

        if (results.isEmpty()) {
            JLabel empty = new JLabel("No recommendations available.");
            empty.setForeground(TEXT);
            resultsPanel.add(empty);
            pageLabel.setText("No tracks available");
        } else {
            int total = results.size();
            offset = offset % total;

            for (int i = 0; i < Math.min(PAGE_SIZE, total); i++) {
                int index = (offset + i) % total;
                Result result = results.get(index);

                resultsPanel.add(createTrackCard(i + 1, result));
                resultsPanel.add(Box.createVerticalStrut(12));
            }

            int first = offset + 1;
            int last = Math.min(offset + PAGE_SIZE, total);

            pageLabel.setText(
                    "Showing " + first + "-" + last
                            + " of " + total + " demo tracks");
        }

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private void showNextPage() {
        List<Result> results = engine.recommend(user);

        if (results.isEmpty()) {
            return;
        }

        offset = (offset + PAGE_SIZE) % results.size();
        statusLabel.setText(" ");
        refreshRecommendations();
    }

    private void saveFeedback(Result result, int rating) {
        int newRating = result.feedback() == rating ? 0 : rating;

        try {
            feedbackRepository.setRating(
                    user.getId(),
                    result.track().title(),
                    newRating
            );

            statusLabel.setForeground(GREEN);
            statusLabel.setText(
                    newRating == 1 ? "Added to liked recommendations."
                            : newRating == -1 ? "Feedback saved."
                            : "Feedback removed."
            );

            refreshRecommendations();

        } catch (SQLException e) {
            statusLabel.setForeground(RED);
            statusLabel.setText("Could not save feedback.");

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to save feedback: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private JPanel createTrackCard(int rank, Result result) {
        JPanel card = new JPanel(new BorderLayout(12, 8));
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(15, 18, 15, 18));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 145));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel rankLabel = new JLabel(String.format("%02d", rank));
        rankLabel.setForeground(GREEN);
        rankLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        rankLabel.setPreferredSize(new Dimension(42, 40));

        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(result.track().title());
        title.setForeground(TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));

        JLabel artist = new JLabel(
                result.track().artist() + " • "
                        + result.track().genre());
        artist.setForeground(MUTED);
        artist.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JLabel reason = new JLabel(result.reason());
        reason.setForeground(GREEN);
        reason.setFont(new Font("SansSerif", Font.PLAIN, 11));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        actions.setOpaque(false);

        JButton like = new JButton(
                result.feedback() == 1 ? "♥ Liked" : "♡ Like");
        like.setFocusPainted(false);
        like.addActionListener(e -> saveFeedback(result, 1));

        JButton dislike = new JButton(
                result.feedback() == -1 ? "Disliked" : "👎 Dislike");
        dislike.setFocusPainted(false);
        dislike.addActionListener(e -> saveFeedback(result, -1));

        actions.add(like);
        actions.add(dislike);

        details.add(title);
        details.add(Box.createVerticalStrut(5));
        details.add(artist);
        details.add(Box.createVerticalStrut(6));
        details.add(reason);
        details.add(Box.createVerticalStrut(8));
        details.add(actions);

        JLabel score = new JLabel(result.score() + "% match");
        score.setForeground(TEXT);
        score.setFont(new Font("SansSerif", Font.BOLD, 13));

        card.add(rankLabel, BorderLayout.WEST);
        card.add(details, BorderLayout.CENTER);
        card.add(score, BorderLayout.EAST);

        return card;
    }
}
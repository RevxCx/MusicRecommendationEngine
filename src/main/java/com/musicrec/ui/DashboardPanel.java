package com.musicrec.ui;

import com.musicrec.model.Song;
import com.musicrec.model.User;
import com.musicrec.repository.SongRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class DashboardPanel extends JPanel {

    private final User user;
    private final JPanel trackList = new JPanel();
    private final JLabel trackCount = new JLabel("0");
    private final JLabel status = new JLabel(" ");
    private final JLabel favoriteMood = new JLabel();
    private final JPanel featuredCard = new JPanel(new BorderLayout(15, 10));

    public DashboardPanel(User user) {
        this.user = user;

        setLayout(new BorderLayout());
        setBackground(UiStyle.BG);
        setBorder(new EmptyBorder(28, 32, 28, 32));

        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));

        JLabel greeting = UiStyle.heading(
                "Welcome back, " + user.getName() + "  ✦", 29
        );

        JLabel subtitle = UiStyle.muted(
                "A little music can change your whole day."
        );

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(greeting);
        header.add(Box.createVerticalStrut(7));
        header.add(subtitle);

        page.add(header);
        page.add(Box.createVerticalStrut(24));

        JPanel stats = new JPanel(new GridLayout(1, 3, 14, 0));
        stats.setOpaque(false);
        stats.add(createStatCard("♫", "LIBRARY", trackCount, "Saved tracks"));
        stats.add(createStatCard("✦", "YOUR MOOD",
                favoriteMood, "Current preference"));
        stats.add(createStatCard("◷", "DISCOVER",
                new JLabel("100"), "Demo tracks available"));

        page.add(stats);
        page.add(Box.createVerticalStrut(22));

        featuredCard.setBackground(new Color(39, 30, 62));
        featuredCard.setBorder(new EmptyBorder(22, 24, 22, 24));
        featuredCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JPanel featuredText = new JPanel();
        featuredText.setOpaque(false);
        featuredText.setLayout(new BoxLayout(featuredText, BoxLayout.Y_AXIS));

        JLabel badge = new JLabel("  YOUR PERSONAL MIX  ");
        badge.setOpaque(true);
        badge.setBackground(new Color(83, 57, 137));
        badge.setForeground(Color.WHITE);
        badge.setFont(new Font("SansSerif", Font.BOLD, 10));

        JLabel featureTitle = UiStyle.heading("Find your next favorite", 21);
        JLabel featureSub = UiStyle.muted(
                "Explore recommendations shaped by your listening preferences."
        );

        featuredText.add(badge);
        featuredText.add(Box.createVerticalStrut(10));
        featuredText.add(featureTitle);
        featuredText.add(Box.createVerticalStrut(5));
        featuredText.add(featureSub);

        JLabel note = new JLabel("♫");
        note.setForeground(UiStyle.ACCENT);
        note.setFont(new Font("SansSerif", Font.BOLD, 52));

        featuredCard.add(featuredText, BorderLayout.CENTER);
        featuredCard.add(note, BorderLayout.EAST);

        page.add(featuredCard);
        page.add(Box.createVerticalStrut(24));

        JPanel listHeader = new JPanel(new BorderLayout());
        listHeader.setOpaque(false);

        JLabel savedTitle = UiStyle.heading("Your library", 21);
        JLabel savedSubtitle = UiStyle.muted("Recently saved tracks");

        JPanel listTitles = new JPanel();
        listTitles.setOpaque(false);
        listTitles.setLayout(new BoxLayout(listTitles, BoxLayout.Y_AXIS));
        listTitles.add(savedTitle);
        listTitles.add(Box.createVerticalStrut(5));
        listTitles.add(savedSubtitle);

        listHeader.add(listTitles, BorderLayout.WEST);

        page.add(listHeader);
        page.add(Box.createVerticalStrut(12));

        trackList.setOpaque(false);
        trackList.setLayout(new BoxLayout(trackList, BoxLayout.Y_AXIS));

        page.add(trackList);
        page.add(Box.createVerticalStrut(12));

        status.setForeground(UiStyle.MUTED);
        status.setFont(new Font("SansSerif", Font.PLAIN, 12));
        page.add(status);

        add(UiStyle.scrollPane(page), BorderLayout.CENTER);

        refreshLibrary();
    }

    private JPanel createStatCard(
            String icon, String label, JLabel value, String description) {

        JPanel card = UiStyle.card();
        card.setLayout(new BorderLayout(8, 8));
        card.setPreferredSize(new Dimension(160, 112));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setForeground(UiStyle.ACCENT);
        iconLabel.setFont(new Font("SansSerif", Font.BOLD, 22));

        JLabel name = new JLabel(label);
        name.setForeground(UiStyle.MUTED);
        name.setFont(new Font("SansSerif", Font.BOLD, 10));

        JPanel top = new JPanel(new BorderLayout(5, 0));
        top.setOpaque(false);
        top.add(iconLabel, BorderLayout.WEST);
        top.add(name, BorderLayout.CENTER);

        value.setForeground(UiStyle.TEXT);
        value.setFont(new Font("SansSerif", Font.BOLD, 23));

        JLabel caption = UiStyle.muted(description);
        caption.setFont(new Font("SansSerif", Font.PLAIN, 11));

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.add(value);
        bottom.add(Box.createVerticalStrut(3));
        bottom.add(caption);

        card.add(top, BorderLayout.NORTH);
        card.add(bottom, BorderLayout.CENTER);
        return card;
    }

    public void refreshLibrary() {
        trackList.removeAll();

        try {
            List<Song> songs = new SongRepository().findAll();

            trackCount.setText(String.valueOf(songs.size()));

            String mood = user.getPreferredMood();
            favoriteMood.setText(
                    mood == null || mood.isBlank() ? "Any mood" : mood
            );

            if (songs.isEmpty()) {
                JPanel empty = UiStyle.card();
                empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));

                JLabel title = UiStyle.heading("Your library is waiting", 17);
                JLabel hint = UiStyle.muted(
                        "Search Spotify and save tracks to build your collection."
                );

                empty.add(title);
                empty.add(Box.createVerticalStrut(7));
                empty.add(hint);

                trackList.add(empty);
                status.setText("Your saved music will appear here.");
            } else {
                int start = Math.max(0, songs.size() - 6);

                for (int i = songs.size() - 1; i >= start; i--) {
                    trackList.add(createTrackRow(songs.get(i)));
                    trackList.add(Box.createVerticalStrut(8));
                }

                status.setText("Showing up to 6 recently saved tracks.");
            }

        } catch (SQLException e) {
            trackCount.setText("—");
            favoriteMood.setText("—");
            status.setText("Couldn't load your library. Please try again.");
        }

        trackList.revalidate();
        trackList.repaint();
    }

    private JPanel createTrackRow(Song song) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(UiStyle.PANEL);
        row.setBorder(new EmptyBorder(12, 15, 12, 15));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JLabel artwork = new JLabel("♫", SwingConstants.CENTER);
        artwork.setOpaque(true);
        artwork.setBackground(new Color(53, 42, 81));
        artwork.setForeground(UiStyle.ACCENT);
        artwork.setFont(new Font("SansSerif", Font.BOLD, 22));
        artwork.setPreferredSize(new Dimension(44, 44));

        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(song.getTitle());
        title.setForeground(UiStyle.TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 13));

        String album = song.getAlbumName();
        if (album == null || album.isBlank()) {
            album = "Unknown album";
        }

        JLabel subtitle = UiStyle.muted(album);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 11));

        details.add(title);
        details.add(Box.createVerticalStrut(4));
        details.add(subtitle);

        JLabel arrow = new JLabel("›");
        arrow.setForeground(UiStyle.MUTED);
        arrow.setFont(new Font("SansSerif", Font.BOLD, 23));

        row.add(artwork, BorderLayout.WEST);
        row.add(details, BorderLayout.CENTER);
        row.add(arrow, BorderLayout.EAST);

        return row;
    }
}
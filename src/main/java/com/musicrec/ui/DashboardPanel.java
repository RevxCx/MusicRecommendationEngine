
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

    private static final Color BG = new Color(18, 18, 18);
    private static final Color PANEL = new Color(30, 30, 30);

    private final User user;
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JLabel status = new JLabel();
    private final JLabel count = new JLabel("0 tracks");

    public DashboardPanel(User user) {
        this.user = user;

        setLayout(new BorderLayout(15, 20));
        setBackground(BG);
        setBorder(new EmptyBorder(30, 35, 30, 35));

        JLabel greeting = new JLabel("Welcome back, " + user.getName());
        greeting.setFont(new Font("SansSerif", Font.BOLD, 30));
        greeting.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
                "Discover music that fits your taste."
        );
        subtitle.setForeground(Color.LIGHT_GRAY);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 16));

        JPanel top = new JPanel();
        top.setBackground(BG);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(greeting);
        top.add(Box.createVerticalStrut(8));
        top.add(subtitle);

        JLabel heading = new JLabel("Your Saved Tracks");
        heading.setFont(new Font("SansSerif", Font.BOLD, 21));
        heading.setForeground(Color.WHITE);

        count.setForeground(Color.LIGHT_GRAY);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);
        header.add(heading, BorderLayout.WEST);
        header.add(count, BorderLayout.EAST);

        JList<String> songs = new JList<>(model);
        songs.setBackground(PANEL);
        songs.setForeground(Color.WHITE);
        songs.setFont(new Font("SansSerif", Font.PLAIN, 14));
        songs.setFixedCellHeight(40);

        status.setForeground(Color.LIGHT_GRAY);
        status.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel center = new JPanel(new BorderLayout(10, 12));
        center.setBackground(BG);
        center.add(header, BorderLayout.NORTH);
        center.add(new JScrollPane(songs), BorderLayout.CENTER);
        center.add(status, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        refreshLibrary();
    }

    public void refreshLibrary() {
        model.clear();

        try {
            List<Song> savedSongs = new SongRepository().findAll();

            for (Song song : savedSongs) {
                String album = song.getAlbumName() == null
                        || song.getAlbumName().isBlank()
                        ? "Unknown album" : song.getAlbumName();

                model.addElement(song.getTitle() + "  —  " + album);
            }

            count.setText(savedSongs.size()
                    + (savedSongs.size() == 1 ? " track" : " tracks"));

            status.setText(savedSongs.isEmpty()
                    ? "No saved tracks yet. Search Spotify and save a track."
                    : "Tracks saved locally in this application.");

        } catch (SQLException e) {
            count.setText("Unavailable");
            status.setText("Couldn't load saved tracks. Please try again.");
        }
    }
}
